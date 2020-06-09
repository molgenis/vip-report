package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.Info;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToInfoMapper {

  public Info map(VCFHeader vcfHeader, Map<String, Object> attributes) {
    Info info = new Info();
    attributes.forEach(
        (key, value) -> {
          Object mappedValue = mapAttribute(vcfHeader, key, value);
          info.put(key, mappedValue);
        });
    return info;
  }

  private static Object mapAttribute(VCFHeader vcfHeader, String key, Object value) {
    Object mappedValue;
    if (vcfHeader.hasInfoLine(key)) {
      VCFInfoHeaderLine infoHeaderLine = vcfHeader.getInfoHeaderLine(key);

      VCFHeaderLineType type = infoHeaderLine.getType();
      switch (type) {
        case Integer:
          mappedValue = mapIntegerAttribute(infoHeaderLine, value);
          break;
        case Float:
          mappedValue = mapFloatAttribute(infoHeaderLine, value);
          break;
        case String:
          mappedValue = mapStringAttribute(infoHeaderLine, value);
          break;
        case Character:
          mappedValue = mapCharacterAttribute(infoHeaderLine, value);
          break;
        case Flag:
          mappedValue = mapFlagAttribute(value);
          break;
        default:
          throw new UnexpectedEnumException(type);
      }
    } else {
      mappedValue = value;
    }
    return mappedValue;
  }

  private static Object mapCharacterAttribute(VCFInfoHeaderLine infoHeaderLine, Object value) {
    return mapStringAttribute(infoHeaderLine, value);
  }

  private static Object mapStringAttribute(VCFInfoHeaderLine infoHeaderLine, Object value) {
    Object mappedValue;
    if (isSingleValue(infoHeaderLine)) {
      mappedValue = getAttributeAsString(value);
    } else {
      mappedValue = getAttributeAsStringList(value);
    }
    return mappedValue;
  }

  private static Object mapFloatAttribute(VCFInfoHeaderLine infoHeaderLine, Object value) {
    Object mappedValue;
    if (isSingleValue(infoHeaderLine)) {
      mappedValue = getAttributeAsDouble(value);
    } else {
      mappedValue = getAttributeAsDoubleList(value);
    }
    return mappedValue;
  }

  private static Object mapIntegerAttribute(VCFInfoHeaderLine infoHeaderLine, Object value) {
    Object mappedValue;
    if (isSingleValue(infoHeaderLine)) {
      mappedValue = getAttributeAsInteger(value);
    } else {
      mappedValue = getAttributeAsIntegerList(value);
    }
    return mappedValue;
  }

  private static boolean isSingleValue(VCFInfoHeaderLine vcfInfoHeaderLine) {
    return vcfInfoHeaderLine.getCountType() == VCFHeaderLineCount.INTEGER
        && vcfInfoHeaderLine.getCount() == 1;
  }

  private static @Nullable Integer getAttributeAsInteger(Object value) {
    Integer integerValue;
    if (value == null || value.equals(VCFConstants.MISSING_VALUE_v4)) {
      integerValue = null;
    } else if (value instanceof Integer) {
      integerValue = (Integer) value;
    } else {
      integerValue = Integer.parseInt((String) value);
    }
    return integerValue;
  }

  private static List<Integer> getAttributeAsIntegerList(Object value) {
    List<Integer> integerValues;
    if (value == null) {
      integerValues = emptyList();
    } else if (value.equals(VCFConstants.MISSING_VALUE_v4)) {
      integerValues = singletonList(null);
    } else if (value instanceof List) {
      integerValues = mapListAttributeAsIntegerList((List<?>) value);
    } else {
      integerValues = mapStringAttributeAsIntegerList((String) value);
    }
    return integerValues;
  }

  private static List<Integer> mapStringAttributeAsIntegerList(String value) {
    List<Integer> integerValues;
    String[] tokens = value.split(",", -1);
    if (tokens.length == 0) {
      integerValues = emptyList();
    } else {
      integerValues = new ArrayList<>(tokens.length);
      for (String token : tokens) {
        Integer integerValue = getAttributeAsInteger(token);
        integerValues.add(integerValue);
      }
    }
    return integerValues;
  }

  private static List<Integer> mapListAttributeAsIntegerList(List<?> values) {
    List<Integer> integerValues;
    if (values.isEmpty()) {
      integerValues = emptyList();
    } else {
      integerValues = new ArrayList<>(values.size());
      for (Object listValue : values) {
        Integer integerValue = getAttributeAsInteger(listValue);
        integerValues.add(integerValue);
      }
    }
    return integerValues;
  }

  private static @Nullable Double getAttributeAsDouble(Object value) {
    Double doubleValue;
    if (value == null || value.equals(VCFConstants.MISSING_VALUE_v4)) {
      doubleValue = null;
    } else if (value instanceof Double) {
      doubleValue = (Double) value;
    } else {
      String stringValue = (String) value;
      doubleValue = Double.parseDouble(stringValue);
    }
    return doubleValue;
  }

  private static List<Double> getAttributeAsDoubleList(Object value) {
    List<Double> doubleValues;
    if (value == null) {
      doubleValues = emptyList();
    } else if (value.equals(VCFConstants.MISSING_VALUE_v4)) {
      doubleValues = singletonList(null);
    } else if (value instanceof List) {
      doubleValues = mapListAttributeAsDoubleList((List<?>) value);
    } else {
      doubleValues = mapStringAttributeAsDoubleList((String) value);
    }
    return doubleValues;
  }

  private static List<Double> mapStringAttributeAsDoubleList(String value) {
    List<Double> doubleValues;
    String[] tokens = value.split(",", -1);
    if (tokens.length == 0) {
      doubleValues = emptyList();
    } else {
      doubleValues = new ArrayList<>(tokens.length);
      for (String token : tokens) {
        Double doubleValue = getAttributeAsDouble(token);
        doubleValues.add(doubleValue);
      }
    }
    return doubleValues;
  }

  private static List<Double> mapListAttributeAsDoubleList(List<?> values) {
    List<Double> doubleValues;
    if (values.isEmpty()) {
      doubleValues = emptyList();
    } else {
      doubleValues = new ArrayList<>(values.size());
      for (Object listValue : values) {
        Double doubleValue = getAttributeAsDouble(listValue);
        doubleValues.add(doubleValue);
      }
    }
    return doubleValues;
  }

  private static @Nullable String getAttributeAsString(Object value) {
    String stringValue;
    if (value == null || value.equals(VCFConstants.MISSING_VALUE_v4)) {
      stringValue = null;
    } else {
      stringValue = (String) value;
    }
    return stringValue;
  }

  private static List<String> getAttributeAsStringList(Object value) {
    List<String> stringValues;
    if (value == null) {
      stringValues = emptyList();
    } else if (value.equals(VCFConstants.MISSING_VALUE_v4)) {
      stringValues = singletonList(null);
    } else if (value instanceof List) {
      stringValues = mapListAttributeAsStringList((List<?>) value);
    } else {
      stringValues = mapStringAttributeAsStringList((String) value);
    }
    return stringValues;
  }

  private static List<String> mapStringAttributeAsStringList(String value) {
    List<String> stringValues;
    String[] tokens = value.split(",", -1);
    if (tokens.length == 0) {
      stringValues = emptyList();
    } else {
      stringValues = new ArrayList<>(tokens.length);
      for (String token : tokens) {
        String stringValue = getAttributeAsString(token);
        stringValues.add(stringValue);
      }
    }
    return stringValues;
  }

  private static List<String> mapListAttributeAsStringList(List<?> values) {
    List<String> stringValues;
    if (values.isEmpty()) {
      stringValues = emptyList();
    } else {
      stringValues = new ArrayList<>(values.size());
      for (Object listValue : values) {
        String stringValue = getAttributeAsString(listValue);
        stringValues.add(stringValue);
      }
    }
    return stringValues;
  }

  private static boolean mapFlagAttribute(Object value) {
    boolean booleanValue;
    if (value == null) {
      booleanValue = false;
    } else if (value instanceof Boolean) {
      booleanValue = (Boolean) value;
    } else {
      String stringValue = (String) value;
      booleanValue = Boolean.parseBoolean(stringValue);
    }
    return booleanValue;
  }
}
