package grader.model.gradebook.gradescheme;
import grader.model.errors.OverlappingRangeException;
import grader.model.items.Percentage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;

/**
 * GradeScheme represents the set of GradeRanges that allow for a student to
 * receive a LetterGrade determined by their Percentage in the class.
 *
 * @author Tobias Bleisch
 */
public class GradeScheme
{
    public final static double ceiling = 100;

    /**
     * Comprised of every available grade range.
     */
    public ArrayList<GradeRange> ranges;

    /**
     * Constructor for fields of GradeScheme which creates and sets
     * a generic GradeScheme to be used in the class.
     */
    public GradeScheme() {
        ranges = new ArrayList<GradeRange>();

        /*
         * Create a generic GradeScheme to be used initially:
         * 90%-100%, A, Green | 80%-90%, B, Blue | 70%-80%, C, Yellow | 60%-70%, D, Purple | 0%-60%, F, Red
         */
        ranges.add(new GradeRange(new Percentage(97.0), LetterGrade.A_PLUS, Color.LIGHTGREEN));
        ranges.add(new GradeRange(new Percentage(93.0), LetterGrade.A, Color.GREEN));
        ranges.add(new GradeRange(new Percentage(90.0), LetterGrade.A_MINUS, Color.DARKGREEN));
        ranges.add(new GradeRange(new Percentage(87.0), LetterGrade.B_PLUS, Color.LIGHTBLUE));
        ranges.add(new GradeRange(new Percentage(83.0), LetterGrade.B, Color.BLUE));
        ranges.add(new GradeRange(new Percentage(80.0), LetterGrade.B_MINUS, Color.DARKBLUE));
        ranges.add(new GradeRange(new Percentage(77.0), LetterGrade.C_PLUS, Color.LIGHTYELLOW));
        ranges.add(new GradeRange(new Percentage(73.0), LetterGrade.C, Color.YELLOW));
        ranges.add(new GradeRange(new Percentage(70.0), LetterGrade.C_MINUS, Color.GOLD));
        ranges.add(new GradeRange(new Percentage(67.0), LetterGrade.D_PLUS, Color.LAVENDER));
        ranges.add(new GradeRange(new Percentage(63.0), LetterGrade.D, Color.PURPLE));
        ranges.add(new GradeRange(new Percentage(60.0), LetterGrade.D_MINUS, Color.DARKVIOLET));
        ranges.add(new GradeRange(new Percentage(0.0), LetterGrade.F, Color.RED));
    }

    /**
     * Updates the GradeRange of the specified LetterGrade with the specified Percentage.
     * @param gradeToUpdate the LetterGrade of the GradeRange to be updated
     * @param newPercent the new Percentage to update the GradeRange with
     * pre:
     *    forall(GradeRange bar;
     *    divisions.contains(bar);
     *    bar != null);
     */
    public void updateGradeRange(LetterGrade gradeToUpdate, Percentage newPercent) throws OverlappingRangeException {
        int index = gradeToUpdate.ordinal();
        Percentage higher = index == 0 ? new Percentage(GradeScheme.ceiling) : ranges.get(index - 1).getLowerBound();
        Percentage lower = ranges.get(gradeToUpdate.ordinal() + 1).getLowerBound();

        if (newPercent.compareTo(higher) >= 0) {
            throw new OverlappingRangeException(String.valueOf(newPercent.getValue()),
                    String.valueOf(higher.getValue()));
        }
        else if (newPercent.compareTo(lower) <= 0) {
            throw new OverlappingRangeException(String.valueOf(newPercent.getValue()),
                    String.valueOf(lower.getValue()));
        }

        ranges.get(gradeToUpdate.ordinal()).setLowerBound(newPercent);
    }

    /**
     * Updates the GradeRange of the specified LetterGrade with the specified Color.
     * @param gradeToUpdate the LetterGrade of the GradeRange to be updated
     * @param newColor the new Color to update the GradeRange with
     */
    public void updateGradeRange(LetterGrade gradeToUpdate, Color newColor) {
        ranges.get(gradeToUpdate.ordinal()).setColor(newColor);
    }

    /**
     * Retrieves the GradeRange in which the specified Percentage falls.
     * @param percent the Percentage from which to find the GradeRange
     * @return the GradeRange in which the specified Percentage falls,
     * or null
     */
    public GradeRange getGradeRange(Percentage percent) {
        GradeRange found = null;

        for (GradeRange range : ranges)
            if (percent.compareTo(range.getLowerBound()) >= 0) {
                found = range;
                break;
            }
        return found;
    }

    /**
     * A String representation of this GradeScheme.
     * @return a String representation of this GradeScheme.
     */
    @Override
    public String toString() {
        return "GradeScheme{" +
                "ranges=" + ranges +
                '}';
    }

   /**
    * Deep copies this GradeScheme.
    * @return deep copy
    */
    public GradeScheme copy() {
       GradeScheme copy = new GradeScheme();
       Collections.copy(copy.ranges, this.ranges);
       return copy;
    }
}
