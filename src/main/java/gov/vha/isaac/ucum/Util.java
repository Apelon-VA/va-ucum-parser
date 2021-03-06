/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ucum;

/**
 * 
 * {@link Util}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Util
{
	protected static boolean isNumeric(String s)
	{
		if (s == null || s.length() == 0)
		{
			return false;
		}
		try
		{
			Float.parseFloat(s);
			return true;
		}
		catch (Exception e)
		{
			try
			{
				Double.parseDouble(s);
				return true;
			}
			catch (Exception e2)
			{
				try
				{
					Integer.parseInt(s);
					return true;
				}
				catch (Exception e3)
				{
					return false;
				}
			}
		}
	}
}
