/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
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
