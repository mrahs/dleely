/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

/**
 * UI Helpers purpose is to split the UI code into several files
 * for several reasons:
 * 1. Simplify build process (avoid having to compile one huge file)
 * 2. Simplify maintenance (have each helper focus on tightly related controls)
 *
 * UI Helpers may interact with each others and usually interact with controllers.
 */
package pw.ahs.app.dleely.gui.helper;