/**
 * @author Quan Tran
 * @author Jon Amireh
 */

package grader.model.items;

import grader.model.errors.PercentageFormatException;
import grader.model.errors.RawScoreFormatException;

import java.time.LocalDate;

/**
 * Represents an assignment of a specific category.
 *
 * @author Jon Amireh
 */
public class Assignment
{
    /** the date and time at which this item is due */
    public LocalDate dueDate;

    /** the name of this particular item */
    public String name;

    /** the number of points this item is worth */
    public int rawPoints;

    /** weight for this particular assignment **/
    public Percentage weight;

    public boolean hasWeight;

    /**
     * Creates a new Assignment based on given parameters
     * @param name String representing the name of the Assignment
     * @param dueDate LocalDate representing the day the Assignment is true
     * @param rawPoints String representing the Double of the Assignment's value
     * @param weight String representing the Percentage of the Assignment's weight
     * @throws PercentageFormatException if the weight given is not a valid Percentage
     * @throws RawScoreFormatException if the rawPoints given is not a valid RawScore
     */
    public Assignment(String name, LocalDate dueDate, String rawPoints, String weight)
    {
        if(name.isEmpty())
        {
            throw new IllegalArgumentException("Please enter a non-empty name");
        }
        else
        {
            this.name = name;
        }
        this.dueDate = dueDate;
        try
        {
            this.rawPoints = Integer.valueOf(rawPoints);
        }
        catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("Please enter raw score as a whole number greater than or equal 0");
        }

        if(this.rawPoints < 0)
        {
            throw new IllegalArgumentException("Please enter raw score as a whole number greater than or equal 0");
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

    public Assignment(String name) {
       this.rawPoints = 100;
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
        this.rawPoints = newValue;
    }

    /**
     * Gets the total points this assignment is worth.
     * @return the total raw points this assignment is worth
     */
    public int getPoints() {
        return rawPoints;
    }

    /**
     * Returns a String representation of this Assignment.
     * @return the name of this Assignment
     */
    public String toString() {
        return name;
    }
}
