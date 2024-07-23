/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.binary.distmap;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		// generic classes
		ChamferDistanceTransform2DFloatTest.class,
		ChamferDistanceTransform2DShortTest.class,
		ChamferDistanceTransform3DFloatTest.class,
		ChamferDistanceTransform3DShortTest.class,
		ChamferMask3DW3FloatTest.class, 
		ChamferMask3DW4Test.class, 
		ChamferMask3DW5Test.class, 
		ChamferMask3DW6Test.class, })
public class AllTests
{
	  //nothing
}
