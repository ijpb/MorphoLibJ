/**
 * 
 */
package inra.ijpb.label.select;

/**
 * Implementation of a relational operator that computes a boolean value from
 * two scalar input values. Declare abstract methods for integer and
 * floating-point comparisons. Common relational operators are provided as
 * static constants.
 * 
 * @author dlegland
 *
 */
public interface RelationalOperator
{
    // ==================================================
    // Static constants

    /**
     * Implementation of the "Greater Than" operator.
     */
    public static final RelationalOperator GT = new RelationalOperator()
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
    };

    /**
     * Implementation of the "Lower Than" operator.
     */
    public static final RelationalOperator LT = new RelationalOperator()
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
    };

    /**
     * Implementation of the "Greater Than Or Equal" operator.
     */
    public static final RelationalOperator GE = new RelationalOperator()
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
    };

    /**
     * Implementation of the "Lower Than Or Equal" operator.
     */
    public static final RelationalOperator LE = new RelationalOperator()
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
    };

    /**
     * Implementation of the "Equal" operator.
     */
    public static final RelationalOperator EQ = new RelationalOperator()
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
    };

    /**
     * Implementation of the "Not Equal" operator.
     */
    public static final RelationalOperator NE = new RelationalOperator()
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
    };


    // ==================================================
    // Abstract methods

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
    
}
