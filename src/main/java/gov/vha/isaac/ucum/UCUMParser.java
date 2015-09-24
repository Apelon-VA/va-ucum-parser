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

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.uomo.units.impl.format.LocalUnitFormatImpl;

/**
 * 
 * A text tokenizing and parsing utility to help extract units of measure from arbitrary strings.
 * {@link UCUMParser}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class UCUMParser
{
	/**
	 * Read an arbitrary string, and return any units of measure (along with their immediately preceeding value)
	 * @param text
	 * @return
	 */
	public static Collection<ParsedUCUM> findUnitsInString(String text)
	{
		HashMap<Integer, ParsedUCUM> results = new HashMap<>();

		Iterator<String> items = tokenize(text).iterator();

		String previous = null;
		String current = null;

		while (!Util.isNumeric(previous) && items.hasNext())
		{
			previous = items.next();
		}
		if (!items.hasNext())
		{
			return results.values();
		}
		current = items.next();

		parseHelper(text, previous, current, results);

		while (items.hasNext())
		{
			previous = current;
			current = items.next();

			while (!Util.isNumeric(previous) && items.hasNext())
			{
				previous = current;
				current = items.next();
			}
			if (previous.equals(current) || !Util.isNumeric(previous))
			{
				continue;
			}
			parseHelper(text, previous, current, results);
		}
		return results.values();
	}
	
	private static void parseHelper(String text, String value, String textToParseForUnit, HashMap<Integer, ParsedUCUM> resultHolder)
	{
		if (StringUtils.isBlank(value) || StringUtils.isBlank(textToParseForUnit) || textToParseForUnit.equals("(") || textToParseForUnit.equals(")"))
		{
			return;
		}
		
		if (textToParseForUnit.equals("H"))
		{
			textToParseForUnit = "h";  //They probably mean hour - not "Henry"
		}
		
		ParsedUCUM potential = uomoParser(value, textToParseForUnit);
		if (potential != null)
		{
			resultHolder.put(potential.hashCode(), potential);
		}
		ParsedUCUM potential2 = jScienceParser(value, textToParseForUnit);
		if (potential2 != null)
		{
			// jScience only provides these 3. Only load the second one if it differs from the one found by uomo
			if (potential == null
					|| !(potential.getName().equals(potential2.getName()) || potential.getDimension().equals(potential2.getDimension()) 
						|| potential.getSystemUnits().equals(potential2.getSystemUnits())))
			{
				resultHolder.put(potential2.hashCode(), potential2);
			}
		}
	}
	
	/**
	 * Execute the Eclipse units of measure parser
	 * @param value
	 * @param textToParseForUnit
	 * @return
	 */
	public static ParsedUCUM uomoParser(String value, String textToParseForUnit)
	{
		try
		{
			// Not sure if this is threadsafe, so no reuse
			LocalUnitFormatImpl uomoParser = new LocalUnitFormatImpl();
			org.unitsofmeasurement.unit.Unit<?> unit = uomoParser.parse(textToParseForUnit, new ParsePosition(0));
			if (unit != null)
			{
				return new ParsedUCUM(textToParseForUnit, value, unit);
			}
		}
		catch (Exception e)
		{
			// noop
		}
		return null;
	}

	/**
	 * Execute the jSciense parser
	 * @param value
	 * @param textToParseForUnit
	 * @return
	 */
	public static ParsedUCUM jScienceParser(String value, String textToParseForUnit)
	{
		try
		{
			// Not sure if this is threadsafe... so no reuse
			javax.measure.unit.UnitFormat jScienceParser = javax.measure.unit.UnitFormat.getUCUMInstance();
			javax.measure.unit.Unit<?> unit = jScienceParser.parseObject(textToParseForUnit, new ParsePosition(0));
			if (unit != null)
			{
				return new ParsedUCUM(textToParseForUnit, value, unit);
			}
		}
		catch (Exception e)
		{
			// noop
		}
		return null;
	}
	
	private static ArrayList<String> tokenize(String string)
	{
		ArrayList<String> tokens = new ArrayList<>();
		
		if (string != null && string.length() > 0)
		{
			Iterator<String> whitespaceSplit = Arrays.asList(string.split("\\s")).iterator();
			while (whitespaceSplit.hasNext())
			{
				String temp = whitespaceSplit.next().trim();
				if (temp.length() == 0)
				{
					continue;
				}
				//subsplit on digits? - aka 'ab5.0mg' into 'ab' 5.0' 'mg'
				
				Boolean readingDigits = null;
				int digitStart = -1;
				int textStart = -1;
				for (int i = 0; i < temp.length(); i++)
				{
					if (Character.isDigit(temp.charAt(i)) || temp.charAt(i) == '.')
					{
						if (readingDigits == null)
						{
							readingDigits = true;
							digitStart = i;
						}
						else if (readingDigits)
						{
							continue;
						}
						else
						{
							readingDigits = true;
							digitStart = i;
							if (textStart != -1 && i > textStart)
							{
								tokens.add(temp.substring(textStart, digitStart));
							}
							textStart = -1;
						}
					}
					else
					{
						if (readingDigits == null)
						{
							readingDigits = false;
							textStart = i;
						}
						else if (readingDigits)
						{
							readingDigits = false;
							textStart = i;
							if (digitStart != -1 && i > digitStart)
							{
								tokens.add(temp.substring(digitStart, textStart));
							}
							digitStart = -1;
						}
						else
						{
							continue;
						}
					}
				}
				
				if (textStart != -1)
				{
					tokens.add(temp.substring(textStart));
				}
				if (digitStart != -1)
				{
					tokens.add(temp.substring(digitStart));
				}
			}
		}
		return tokens;
	}
}
