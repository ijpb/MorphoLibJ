/**
 * 
 */
package inra.ijpb.label.select;

/**
 * Implementation of a relational operator that computes a boolean value from
 * two scalar input values. Declare abstract classes for integer and
 * floating-point comparisons.
 * 
 * @author dlegland
 *
 */
public interface RelationalOperator
{
    /**
     * Evaluates this operator from two integer values.
     * 
     * @param value1
     *            the first integer value.
     * @param value2
     *            the second integer value.
     * @return the result of the relational operator.
     */
    public boolean evaluate(int value1, int value2);
    
    /**
     * Evaluates this operator from two floating-point values.
     * 
     * @param value1
     *            the first floating-point value.
     * @param value2
     *            the second floating-point value.
     * @return the result of the relational operator.
     */
    public boolean evaluate(double value1, double value2);
    
    /**
     * Implementation of the "Greater Than" operator.
     */
    public static final class GT implements RelationalOperator
    {
        @Override
        public boolean evaluate(int value1, int value2)
        {
            return value1 > value2;
        }

        @Override
        public boolean evaluate(double value1, double value2)
        {
            return value1 > value2;
        }
    }

    /**
     * Implementation of the "Lower Than" operator.
     */
    public static final class LT implements RelationalOperator
    {
        @Override
        public boolean evaluate(int value1, int value2)
        {
            return value1 < value2;
        }

        @Override
        public boolean evaluate(double value1, double value2)
        {
            return value1 < value2;
        }
    }

    /**
     * Implementation of the "Greater Than Or Equal" operator.
     */
    public static final class GE implements RelationalOperator
    {
        @Override
        public boolean evaluate(int value1, int value2)
        {
            return value1 >= value2;
        }

        @Override
        public boolean evaluate(double value1, double value2)
        {
            return value1 >= value2;
        }
    }

    /**
     * Implementation of the "Lower Than Or Equal" operator.
     */
    public static final class LE implements RelationalOperator
    {
        @Override
        public boolean evaluate(int value1, int value2)
        {
            return value1 <= value2;
        }

        @Override
        public boolean evaluate(double value1, double value2)
        {
            return value1 <= value2;
        }
    }

    /**
     * Implementation of the "Equal" operator.
     */
    public static final class EQ implements RelationalOperator
    {
        @Override
        public boolean evaluate(int value1, int value2)
        {
            return value1 == value2;
        }

        @Override
        public boolean evaluate(double value1, double value2)
        {
            return value1 == value2;
        }
    }

    /**
     * Implementation of the "Not Equal" operator.
     */
    public static final class NE implements RelationalOperator
    {
        @Override
        public boolean evaluate(int value1, int value2)
        {
            return value1 != value2;
        }

        @Override
        public boolean evaluate(double value1, double value2)
        {
            return value1 != value2;
        }
    }

}
