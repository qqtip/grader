/**
 * @author Quan Tran
 * @author Jon Amireh
 */

package grader.model.items;

import grader.model.errors.PercentageFormatException;
import grader.model.errors.RawScoreFormatException;
import grader.model.gradebook.Percentage;

import java.time.LocalDate;

/**
 * Represents an assignment of a specific category.
 */
public class Assignment
{
    /** the date and time at which this item is due */
    LocalDate dueDate;

    /** the name of this particular item */
    String name;

    /** the number of points this item is worth */
    int rawPoints;

    /** weight for this particular assignment **/
    Percentage weight;

    boolean hasWeight;

    public Assignment(String name, LocalDate dueDate, String rawPoints, String weight) throws PercentageFormatException, RawScoreFormatException
    {
        this.name = name;
        this.dueDate = dueDate;
        try
        {
            this.rawPoints = Integer.valueOf(rawPoints);
        }
        catch(NumberFormatException e)
        {
            throw new RawScoreFormatException(rawPoints);
        }
        if(this.rawPoints < 0)
        {
            throw new RawScoreFormatException(this.rawPoints);
        }
        if(!weight.isEmpty())
        {
            this.weight = new Percentage(weight);
            this.hasWeight = true;
        }
        else
        {
            this.weight = null;
            this.hasWeight = false;
        }
    }

    public Assignment() {}
    public Assignment(String name) {
       this.name = name;
    }

    /**
     * Adjusts an item's point value.
     * @param newValue the new value to adjust to
     post:
       // the raw point value of this Item is adjusted
       this.rawPoints == newValue
     */
    public void adjustPointValue(int newValue)
    {

    }

    /**
     * Gets the total points this assignment is worth.
     * @return the total raw points this assignment is worth
     */
    public int getPoints() {
        return rawPoints;
    }
}
