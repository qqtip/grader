package grader.model.gradebook;

import grader.controller.SidebarController;
import grader.controller.SpreadsheetController;
import grader.model.curve.Histogram;
import grader.model.curve.PieChart;
import grader.model.gradebook.gradescheme.GradeScheme;
import grader.model.gradebook.scores.RawScore;
import grader.model.gradebook.scores.Scores;
import grader.model.gradebook.stats.StatsContainer;
import grader.model.items.Assignment;
import grader.model.items.AssignmentTree;
import grader.model.items.Category;
import grader.model.people.Group;
import grader.model.people.Student;

import java.util.*;

/**
 * The WorkSpace class is a singleton that contains all the information for the
 * current user session, including the open Gradebook, active user,
 * undo/redo history, and the clipboard.
 * <p>
 * The WorkSpace provides methods for interacting with the grades relevant to
 * the scope (or context) of the grades being considered.  The scope is
 * controlled by the grader sidebar via the sidebarSelect method.  Upon scope
 * updates, all of the GUI underlying models are notified of the change, at
 * which time they will query for the information they need to display to the
 * user.
 * <p>
 * The Scores object held by the workspace is a copy of the grades relevant
 * to the current scope.  This copy is necessary to maintain temporary changes
 * to scores before they are persisted to the gradebook's scores object.  This
 * way, reverting the scores is a simple matter of reloading the scores from
 * the gradebook.  Saving the scores iterates through the list of deltas and
 * commits them to the gradebook.  Undo and redo are handled by maintaining a
 * list of future deltas.  Deltas move between these lists during undo/redo
 * operations.
 *
 * @author Gregory Davis
 */
public class WorkSpace extends Observable {
    /**
     * Singleton WorkSpace instance.
     */
    public static final WorkSpace instance = new WorkSpace();

    static {
        instance.setChanged();
        instance.notifyObservers();
    }

    /**
     * Constructor.
     * Instantiates necessary models.
     */
    private WorkSpace() {
        gradebook = Gradebook.getCannedGradebook();
        deltas = new ArrayList<RawScore>();
        futureDeltas = new ArrayList<RawScore>();
        gradeScheme = null;

        statistics = new StatsContainer();
        pieChart = new PieChart();
        histogram = new Histogram();

        this.selectedScore = null;
        this.copiedScore = 0.0;
        this.validCopy = false;

        addObserver(statistics);
        addObserver(pieChart);
        addObserver(histogram);
    }

    /////////////////////
    /* CLIPBOARD STUFF */
    /////////////////////

    /**
     * The contents of the currently selected item.
     */
    public RawScore selectedScore;

    /**
     * The most recently copied or cut score.
     */
    public double copiedScore;

    /**
     * Whether the copiedScore is valid.
     */
    public boolean validCopy;

    ////////////////////////////////////


    /////////////////////////////
    /* CURRENT GRADEBOOK SCOPE */
    /////////////////////////////
    /**
     * The currently open Gradebook.
     */
    public Gradebook gradebook;

    /**
     * The Course currently selected in the sidebar.
     */
    public Course course;

    /**
     * The Section currently selected in the sidebar.
     */
    public Section section;

    /**
     * The Group currently selected in the sidebar.
     */
    public Group group;

    /**
     * The Scores currently displayed in the grade spreadsheet.
     */
    public Scores scores;

    /**
     * A list of new raw score changes to be applied to the
     * grade spreadsheet.
     */
    public List<RawScore> deltas;

    /**
     * A list of undone deltas for undo/redo operations.
     */
    public List<RawScore> futureDeltas;

    /**
     * Temporary GradeScheme for scope.
     */
    public GradeScheme gradeScheme;

    /**
     * Flag for GradeScheme changes.
     */
    public boolean gradeSchemeChanged;
    /////////////////////////////


    //////////////////////
    /* COMPONENT MODELS */
    //////////////////////
    /**
     * The statistics model.
     */
    public StatsContainer statistics;

    /**
     * The pie chart model.
     */
    public PieChart pieChart;

    /**
     * The histogram model.
     */
    public Histogram histogram;
    //////////////////////


    /////////////////////////////////
    /* QUERY METHODS FOR OBSERVERS */
    /////////////////////////////////

    /**
     * Returns a reference to the currently open gradebook.
     *
     * @return current gradebook
     */
    public Gradebook getGradebook() {
        return gradebook;
    }

    /**
     * Returns the currently selected course.
     *
     * @return selected course
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Returns the currently selected section.
     *
     * @return selected section
     */
    public Section getSection() {
        return section;
    }

    /**
     * Returns the currently built pie chart
     *
     * @return pie chart model
     */
    public PieChart getPieChart() {
        return pieChart;
    }

    /**
     * Returns the currently built histogram
     *
     * @return selected histogram
     */
    public Histogram getHistogram() {
        return histogram;
    }

    /**
     * Returns the currently selected group.
     *
     * @return selected group
     */
    public Group getGroup() {
        return group;
    }

    public Student selectedStudent;

    /**
     * Adds a group to the currently selected section
     */
    public void addGroup(Group g) {
        getSection().addGroup(g);
        setChanged();
        notifyObservers();
    }

    /**
     * Returns a list of students whose grades are being displayed
     * in the grade spreadsheet.
     * Returns an empty list if nothing is in scope.
     *
     * @return list of students in scope
     */
    public List<Student> getStudents() {
        if (group != null) return group.getStudents();
        if (section != null) return section.getStudents();
        if (course != null) return course.getStudents();
        return new ArrayList<Student>();
    }

    /**
     * Adds a student to the selected section.
     * @param s student to add
     */
    public void addStudent(Student s) {
        if (section != null) {
            section.addStudent(s);
            Iterator ai = getAssignmentTree().getAssignmentIterator();
            while (ai.hasNext()) {
                gradebook.getScores().addRawScore(s, (Assignment) ai.next(), 0.0);
            }
            loadScores();
        }
    }

    /**
     * Returns a map of the assignments of which grades are being
     * displayed in the grade spreadsheet.
     * Returns an empty tree if nothing is in scope.
     *
     * @return current course assignment tree
     */
    public AssignmentTree getAssignmentTree() {
        if (course != null) return course.getAssignmentTree();
        return new AssignmentTree();
    }

    /**
     * Returns the scores object for the scores being displayed
     * in the grade spreadsheet.
     * Returns an empty scores object if nothing is in scope.
     *
     * @return scores map for students and assignments in scope
     */
    public Scores getScores() {
        if (scores != null) return scores;
        return new Scores();
    }

    /**
     * Returns the grade scheme for the currently selected section.
     * Returns an empty grade scheme if no section is in scope.
     *
     * @return grade scheme for section in scope
     */
    public GradeScheme getGradeScheme() {
        return gradeScheme;
    }
    /////////////////////////////////


    //////////////////////////////////
    /* UPDATE METHODS FOR OBSERVERS */
    //////////////////////////////////

    /**
     * Selects a given course, section, and group for the current spreadsheet.
     * Parameters can be null, but only from the bottom up.  For example, if a
     * section is null, the group must also be null.  This would mean that the
     * course is selected in the sidebar, but not any of its specific sections.
     *
     * @param course newly selected course
     * @param section newly selected section
     * @param group newly selected group
     *                                                             <pre>
     post:
       //
       // The workspace scope should reflect the update.
       //
       this'.course.equals(course) && this'.section.equals(section)
       && this'.group.equals(group);
     */
    public void sidebarSelect(Course course, Section section,
                              Group group) {
        this.course = course;
        this.section = section;
        this.group = group;
        this.selectedStudent = null;

        loadScores();
        loadGradeScheme();

        setChanged();
        notifyObservers();
    }

    public void update() {
        sidebarSelect(course, section, group);
    }

    /**
     * Sets the selected score for copying/cutting/pasting.
     *
     * @param score selected score
     */
    public void setSelectedScore(RawScore score) {
        this.selectedScore = score;
    }

    /**
     * Sets the selected student for the MenuBar.
     *
     * @param s selected student
     */
    public void setSelectedStudent(Student s) {
        this.selectedStudent = s;
        setChanged();
        notifyObservers(new Class[]{SpreadsheetController.class, SidebarController.class});
    }

    /**
     * Adds an assignment to the course.
     *
     * @param parent parent category
     * @param a      assignment to add
     */
    public void addAssignment(Category parent, Assignment a) {
        if (course != null) {
            for (Student s : course.getStudents()) {
                gradebook.getScores().addRawScore(s, a, 0.0);
            }
            course.addAssignment(parent, a);
            loadScores();
        }
    }

    /**
     * Creates a delta for a score for the given student and assignment.
     * The temporary Scores object is also updated to reflect current changes.
     * Deltas are not saved to persistent storage until the user saves them.
     *
     * @param student    student whose grade to update
     * @param assignment assignment grade to update
     * @param score      new score
     *                   <pre>
     pre:
       //
       // The student and assignment to add a score for must be in scope.
       //
       getStudents().contains(student)
       && getAssignmentTree().contains(assignment);

     post:
       //
       // The workspace scores must reflect the change, and there must be a
       // delta for the score change.
       // The future deltas list should also be cleared.
       //
       deltas'.get(deltas.size() - 1).getStudent().equals(student)
       && deltas'.get(deltas.size() - 1).getAssignment().equals(assignment)
       && futureDeltas'.size() == 0
       && Double.compare(this'.getScores().getRawScore(student, assignment),
                         score) == 0;
     */
    public void updateGrade(Student student, Assignment assignment,
                            double score) {
        RawScore raw = new RawScore(student, assignment, score);
        futureDeltas.clear();
        deltas.add(raw);
        scores.updateRawScore(student, assignment, score);
        setChanged();
        notifyObservers();
    }

    /**
     * Reverts all deltas and restores the Scores object to its state prior to
     * the changes.
     * <pre>
     post:
       //
       // The current workspace deltas should be empty and the grades in the
       // scores should be equal to their corresponding gradebook scores.
       //
       deltas.size() == 0
       &&
       futureDeltas.size() == 0
       &&
       forall (Student student; this'.getStudents().contains(student);
         forall (Assignment assignment;
                 this'.getAssignmentTree().contains(assignment);
                 Double.compare(
                   gradebook.getScores().getRawScore(student, assignment),
                   this'.getScores().getRawScore(student, assignment)) == 0));
     */
    public void revertGrades() {
        deltas.clear();
        futureDeltas.clear();

        loadScores();

        setChanged();
        notifyObservers();
    }

    /**
     * Commits all deltas to persistent storage.
     * <pre>
     post:
       //
       // All changes reflected in the deltas should be saved to the gradebook
       // and the deltas and future deltas should be cleared.
       //
       forall (RawScore rawScore; deltas.contains(rawScore);
               Double.compare(gradebook'.getScores().getRawScore(
                   rawScore.getStudent(),
                   rawScore.getAssignment()),
                   rawScore.getScore()) == 0)
       && deltas'.size() == 0
       && futureDeltas'.size() == 0;
     */
    public void saveGrades() {
        Scores gradebookScores = gradebook.getScores();
        for (RawScore raw : deltas) {
            gradebookScores.updateRawScore(
                    raw.getStudent(), raw.getAssignment(), raw.getScore());
        }
        deltas.clear();
        futureDeltas.clear();
        loadScores();

        setChanged();
        notifyObservers();
    }

    /**
     * Sets the GradeScheme changed flag and notifies observers.
     */
    public void setGradeSchemeChanged() {
        gradeSchemeChanged = true;
        setChanged();
        notifyObservers();
    }

    /**
     * Commits temporary GradeScheme changes to the section.
     * <pre>
     post:
       //
       // The workspace section should have the new grade scheme.
       //
       this'.section.getGradeScheme().equals(gradeScheme);
     */
    public void updateGradeScheme() {
        if (gradeSchemeChanged && section != null) {
            section.setGradeScheme(this.gradeScheme);
            gradeSchemeChanged = false;
            loadGradeScheme();
            setChanged();
            notifyObservers();
        }
    }
    //////////////////////////////////


    /////////////////////
    /* EDIT OPERATIONS */
    /////////////////////

    /**
     * Returns whether a change can be undone.
     *
     * @return whether there are any deltas to undo
     */
    public boolean canUndo() {
        return !deltas.isEmpty();
    }

    /**
     * Returns whether a change can be redone.
     *
     * @return whether there are any undone deltas to redo
     */
    public boolean canRedo() {
        return !futureDeltas.isEmpty();
    }

    /**
     * Undoes a change by removing a delta.
     * <pre>
     * pre:
     * //
     * // There is a delta to be undone.
     * //
     * canUndo() == true;
     *
     post:
       //
       // The last item in the old deltas list should now be at the end of
       // the future deltas list. The corresponding score should be reverted
       // to the gradebook score.
       //
       deltas'.size() == deltas.size() - 1;
       &&
       futureDeltas'.size() == futureDeltas.size() + 1;
       &&
       forall(RawScore rawScore; deltas'.contains(rawScore)
               iff deltas.contains(rawScore) &&
               !rawScore.equals(deltas.get(deltas.size() - 1)))
       &&
       forall(RawScore rawScore; futureDeltas'.contains(rawScore)
               iff futureDeltas.contains(rawScore) ||
               rawScore.equals(deltas.get(deltas.size() - 1)))
       &&
       Double.compare(
           this'.getScores().getRawScore(
               deltas.get(deltas.size() - 1).getStudent(),
               deltas.get(deltas.size() - 1).getAssignment()),
           gradebook.getScores().getRawScore(
               deltas.get(deltas.size() - 1).getStudent(),
               deltas.g/et(deltas.size() - 1).getAssignment())) == 0;
     */
    public void undo() {
        if (canUndo()) {
            RawScore undoneDelta = deltas.get(deltas.size() - 1);
            deltas.remove(deltas.size() - 1);
            futureDeltas.add(undoneDelta);

            Student student = undoneDelta.getStudent();
            Assignment assignment = undoneDelta.getAssignment();

            // Revert score.
            scores.updateRawScore(student, assignment,
                    gradebook.getScores().getRawScore(student, assignment));
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Redoes an undone change.
     * <pre>
     * pre:
     * //
     * // There is a future delta to be redone.
     * //
     * canRedo() == true;
     *
     post:
       //
       // The last item in the old future deltas list should now be at the end of
       // the deltas list, and the scores should reflect the change.
       //
       deltas'.size() == deltas.size() + 1;
       &&
       futureDeltas'.size() == futureDeltas.size() - 1;
       &&
       forall(RawScore rawScore; deltas'.contains(rawScore)
               iff deltas.contains(rawScore) ||
               rawScore.equals(futureDeltas.get(futureDeltas.size() - 1)))
       &&
       forall(RawScore rawScore; futureDeltas'.contains(rawScore)
               iff futureDeltas.contains(rawScore) &&
               !rawScore.equals(futureDeltas.get(futureDeltas.size() - 1)))
       &&
       Double.compare(
           this'.getScores().getRawScore(
               deltas.get(deltas.size() - 1).getStudent(),
               deltas.get(deltas.size() - 1).getAssignment()),
           futureDeltas.get(futureDeltas.size() - 1).getScore()) == 0;
     */
    public void redo() {
        if (canRedo()) {
            RawScore redoneDelta = futureDeltas.get(futureDeltas.size() - 1);
            futureDeltas.remove(futureDeltas.size() - 1);
            deltas.add(redoneDelta);

            // Reupdate score.
            scores.updateRawScore(redoneDelta.getStudent(),
                    redoneDelta.getAssignment(), redoneDelta.getScore());

            setChanged();
            notifyObservers();
        }
    }

    /**
     * Gets the latest workspace delta.
     * Returns null if cannot undo.
     *
     * @return latest delta
     */
    public RawScore getLatestChange() {
        if (canUndo()) {
            return deltas.get(deltas.size() - 1);
        }
        return null;
    }

    /**
     * Gets the latest undone workspace delta.
     * Returns null if cannot redo.
     *
     * @return latest undone delta
     */
    public RawScore getLatestUndo() {
        if (canRedo()) {
            return futureDeltas.get(futureDeltas.size() - 1);
        }
        return null;
    }

    /**
     * Checks whether a value can be copied or cut.
     *
     * @return whether a copy/cut can be performed
     */
    public boolean canCopy() {
        return selectedScore != null;
    }

    /**
     * Checks whether a value can be pasted.
     *
     * @return whether a paste can be performed
     */
    public boolean canPaste() {
        return validCopy && selectedScore != null;
    }

    /**
     * Copies the value of the selected score to the clipboard.
     */
    public void copy() {
        if (canCopy()) {
            copiedScore = selectedScore.getScore();
            validCopy = true;
        } else {
            validCopy = false;
        }
    }

    /**
     * Cuts the selected score, updating the selected grade to zero.
     */
    public void cut() {
        if (canCopy()) {
            copiedScore = selectedScore.getScore();
            updateGrade(selectedScore.getStudent(), selectedScore.getAssignment(), 0.0);
            validCopy = true;
        } else {
            validCopy = false;
        }
    }

    /**
     * Pastes the previously copied score to the selected RawScore.
     */
    public void paste() {
        if (canPaste()) {
            updateGrade(selectedScore.getStudent(), selectedScore.getAssignment(), copiedScore);
        }
    }

    /////////////////////


    /////////////////////
    /* PRIVATE METHODS */
    /////////////////////

    /**
     * Loads scores for the students in scope from the gradebook.
     * <pre>
     post:
       //
       // The workspace scores should all be equal to the gradebook scores.
       //
       deltas'.size() == deltas.size() + 1;
       &&
       futureDeltas'.size() == futureDeltas.size() - 1;
       &&
       forall(RawScore rawScore; deltas'.contains(rawScore)
               iff deltas.contains(rawScore) ||
               rawScore.equals(futureDeltas.get(futureDeltas.size() - 1)))
       &&
       forall(RawScore rawScore; futureDeltas'.contains(rawScore)
               iff futureDeltas.contains(rawScore) &&
               !rawScore.equals(futureDeltas.get(futureDeltas.size() - 1)))
       &&
       Double.compare(
           this'.getScores().getRawScore(
               deltas.get(deltas.size() - 1).getStudent(),
               deltas.get(deltas.size() - 1).getAssignment()),
           futureDeltas.get(futureDeltas.size() - 1).getScore()) == 0;
     */
    private void loadScores() {
        Scores gradebookScores = gradebook.getScores();
        List<Student> scopedStudents = getStudents();
        scores = new Scores();

        // Add in scores for relevant students.
        for (Student student : scopedStudents) {
            HashMap<Assignment, RawScore> scoresMap = gradebookScores.getScoresMap(student);
            scores.addScoresMap(student, scoresMap);
        }
        scores = scores.copy();
    }

    /**
     * Loads a copy of the section's GradeScheme.
     */
    private void loadGradeScheme() {
        gradeSchemeChanged = false;
        if (section != null) {
            this.gradeScheme = section.getGradeScheme().copy();
        } else {
            this.gradeScheme = null;
        }
    }
}