package grader.model.gradebook;

import grader.model.file.WorkSpace;
import grader.model.items.Assignment;
import grader.model.items.AssignmentTree;
import grader.model.people.Instructor;
import grader.model.people.Name;
import grader.model.people.Student;
import org.omg.CORBA.DynAnyPackage.Invalid;

import javax.naming.InvalidNameException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
*This class collects all the various components of the grader: Instructor, TA and Student Views.
**/
public class Gradebook {

	/**
	*The courses associated with this gradebook.
	*/
	public ArrayList<Course> courses;

	/**
	*The Instructor associated with this gradebook.
	*No limits on permissions.
	*/
	public Instructor instructor;

	/**
	 * The gradebook's master score table.
	 */
	public Scores scores;

	/**
	*Set Instructor associated with this gradebook.
	*pre:
	*	instructor.equals(null);
	*/
	public void setInstructor(Instructor ins) {
	   this.instructor = ins;
   }

	/**
	*Add courses to collection associated with this gradebook.
	*pre:
	*	!courses.contains(cor);
	*/
	public void addCourse(Course cor) {
	   courses.add(cor);
   }

	/**
	 * Returns the master scores object for the gradebook.
	 */
	public Scores getScores() {
	   return scores;
   }

   private static Gradebook cannedGradebook;

   public Gradebook()
   {
      courses = new ArrayList<Course>();
   }

   static {
      cannedGradebook = new Gradebook();
      Course course = new Course("CPE 309");
      Section section = new Section("01");
      course.addSection(section);

      String[] names = {
         "Brandon Clark",
         "Carmen Dang",
         "Vivian Fong",
         "Nupur Garg",
         "Katelyn Hicks",
         "Helen Hwang",
         "Esha Joshi",
         "Connor Krier",
         "Daniel Lee",
         "Myra Lukens",
         "Blaine Oelkers",
         "Frank Poole",
         "Brian Quezada",
         "Wasae Qureshi",
         "Daniel Toy"
      };

      String[] assignmentNames = {
         "Project 1",
         "Project 2",
         "Quiz 1",
         "Quiz 2",
         "Midterm 1",
         "Midterm 2",
         "Final",
         "Participation"
      };

      List<Student> students = new ArrayList<Student>();
      List<Assignment> assignments = new ArrayList<Assignment>();

      for (String name : Arrays.asList(names)) {
         String[] tokens = name.split(" ");
          Student student = null;
          try
          {
              student = new Student(new Name(tokens[0], "", tokens[1]));
          }
          catch (InvalidNameException e)
          {
              System.out.println("name exception occured in gradebook");
          }
         students.add(student);
         section.addStudent(student);
      }

      for (String assignmentName : Arrays.asList(assignmentNames)) {
         Assignment assignment = new Assignment(assignmentName);
         assignments.add(assignment);
         course.addAssignment(assignment);
      }

      Scores scores = new Scores();

      for (Student student : students) {
         for (Assignment assignment : assignments)  {
            scores.addRawScore(student, assignment, 0.0);
         }
      }

      cannedGradebook.scores = scores;
      cannedGradebook.addCourse(course);
      AssignmentTree.AssignmentIterator itr =
         course.getAssignmentTree().getAssignmentIterator();

      System.out.println("Canned gradebook assignments:");
      while (itr.hasNext()) {
          System.out.println(itr.next());
      }

      itr =
         course.getAssignmentTree().getAssignmentIterator();

      System.out.println("Canned gradebook assignments:");
      while (itr.hasNext()) {
          System.out.println(itr.next());
      }

   }

   /**
    * Returns the gradebook with canned data.
    */
   public static Gradebook getCannedGradebook() {
      return cannedGradebook;
   }
}

