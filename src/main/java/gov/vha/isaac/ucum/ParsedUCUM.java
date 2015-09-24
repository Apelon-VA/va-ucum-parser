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

import java.util.Arrays;
import java.util.Optional;

/**
 * A class to carry parsed UCUM results back, independent of underlying API implementations
 * {@link ParsedUCUM}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ParsedUCUM
{
	//TODO rework this class, to have types better than strings?
	private String value_, parsedText_, name_, symbol_, dimension_, systemUnits_;

	public ParsedUCUM(String parsedText, String value, javax.measure.unit.Unit<?> unit)
	{
		parsedText_ = parsedText;
		value_ = value;
		name_ = unit.toString();
		//no symbol from this API?
		dimension_ = unit.getDimension().toString();
		systemUnits_ = unit.getStandardUnit().toString();
		check();
	}

	public ParsedUCUM(String parsedText, String value, org.unitsofmeasurement.unit.Unit<?> unit)
	{
		parsedText_ = parsedText;
		value_ = value;
		name_ = unit.toString();
		symbol_ = unit.getSymbol();
		dimension_ = unit.getDimension().toString();
		systemUnits_ = unit.getSystemUnit().toString();
		check();
	}

	private void check()
	{
		if (Util.isNumeric(name_))
		{
			throw new IllegalArgumentException();
		}
		if (name_ == null || name_.length() == 0)
		{
			throw new IllegalArgumentException();
		}
		if (name_.equals("%") || name_.equals("'") || name_.equals("\""))
		{
			throw new IllegalArgumentException();
		}
		if (name_.equals("a") || name_.equals("c")) //atto, speed of light...
		{
			throw new IllegalArgumentException();
		}
		if (name_.equals("grade") || name_.equals("K")) //odd one... , Kelvin - which doesn't get used as far as I can see
		{
			throw new IllegalArgumentException();
		}
		if (name_.equals("rd")) //an incorrect abbreviation for rad, which is wrong in the cases I looked at
		{
			throw new IllegalArgumentException();
		}
		if (dimension_ != null && dimension_.contains("I"))  //CURRENT
		{
			throw new IllegalArgumentException("Curent is unlikely in SCT");
		}
	}
	
	public static String[] getColumnNames()
	{
		return new String[] { "Value", "Parsed Text", "Name", "Symbol", "Dimension", "System Units"};
	}

	public String[] getRowData()
	{
		return new String[] { (value_ == null ? "" : value_), (parsedText_ == null ? "" : parsedText_), (name_ == null ? "" : name_), (symbol_ == null ? "" : symbol_),
				(dimension_ == null ? "" : dimension_), (systemUnits_ == null ? "" : systemUnits_)};
	}

	public String getValue()
	{
		return value_;
	}

	public void setValue(String value)
	{
		this.value_ = value;
	}

	public String getParsedText()
	{
		return parsedText_;
	}

	public void setParsedText(String parsedText)
	{
		this.parsedText_ = parsedText;
	}

	public String getName()
	{
		return name_;
	}

	public void setName(String name)
	{
		this.name_ = name;
	}

	public Optional<String> getSymbol()
	{
		return Optional.ofNullable(symbol_);
	}

	public void setSymbol(String symbol)
	{
		this.symbol_ = symbol;
	}

	public String getDimension()
	{
		return dimension_;
	}

	public void setDimension(String dimension)
	{
		this.dimension_ = dimension;
	}

	public String getSystemUnits()
	{
		return systemUnits_;
	}

	public void setSystemUnits(String systemUnits)
	{
		this.systemUnits_ = systemUnits;
	}

	@Override
	public int hashCode()
	{
		return Arrays.toString(getRowData()).hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return Arrays.toString(getRowData()).equals(obj);
	}
}
