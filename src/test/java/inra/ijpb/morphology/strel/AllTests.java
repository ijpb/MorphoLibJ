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
package inra.ijpb.morphology.strel;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// First linear strels
	LinearHorizontalStrelTest.class, 
	LinearVerticalStrelTest.class,
	LinearDiagDownStrelTest.class, 
	LinearDiagUpStrelTest.class,
	LinearDepthStrel3DTest.class,
	// compound of linear 
	SquareStrelTest.class, 
	OctagonStrelTest.class,
	// add crosses and diamonds
	Cross3x3StrelTest.class, 
	ShiftedCross3x3Strel_LeftTest.class,
	ShiftedCross3x3Strel_RightTest.class,
	DiamondStrelTest.class,
	// Also Disk strel, based on rank filters
	DiskStrelTest.class,
})
public class AllTests {
  //nothing
}
