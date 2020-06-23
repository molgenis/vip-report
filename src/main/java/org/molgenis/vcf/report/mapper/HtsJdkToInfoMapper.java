package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import htsjdk.variant.vcf.VCFConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.Info;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata.Type;
import org.molgenis.vcf.report.model.metadata.Number;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToInfoMapper {

  public Info map(List<InfoMetadata> infoMetadataList, Map<String, Object> attributes) {
    Info info = new Info();
    attributes.forEach(
        (key, value) -> {
          InfoMetadata infoMetadata =
              infoMetadataList.stream()
                  .filter(anInfoMetadata -> anInfoMetadata.getId().equals(key))
                  .findFirst()
                  .orElse(null);
          Object mappedValue = mapAttribute(infoMetadata, value);
          info.put(key, mappedValue);
        });
    return info;
  }

  private Object mapAttribute(InfoMetadata infoMetadata, Object value) {
    Object mappedValue;
    if (infoMetadata != null) {
      @NonNull Type type = infoMetadata.getType();
      switch (type) {
        case CHARACTER:
          mappedValue = mapCharacterAttribute(infoMetadata, value);
          break;
        case INTEGER:
          mappedValue = mapIntegerAttribute(infoMetadata, value);
          break;
        case FLAG:
          mappedValue = mapFlagAttribute(value);
          break;
        case FLOAT:
          mappedValue = mapFloatAttribute(infoMetadata, value);
          break;
        case STRING:
          mappedValue = mapStringAttribute(infoMetadata, value);
          break;
        case NESTED:
          mappedValue = mapNestedAttribute(infoMetadata, value);
          break;
        default:
          throw new UnexpectedEnumException(type);
      }
    } else {
      mappedValue = value;
    }
    return mappedValue;
  }

  private static Object mapCharacterAttribute(InfoMetadata infoMetadata, Object value) {
    return mapStringAttribute(infoMetadata, value);
  }

  private static Object mapStringAttribute(InfoMetadata infoMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(infoMetadata)) {
      mappedValue = getAttributeAsString(value);
    } else {
      mappedValue = getAttributeAsStringList(infoMetadata, value);
    }
    return mappedValue;
  }

  private static Object mapFloatAttribute(InfoMetadata infoMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(infoMetadata)) {
      mappedValue = getAttributeAsDouble(value);
    } else {
      mappedValue = getAttributeAsDoubleList(infoMetadata, value);
    }
    return mappedValue;
  }

  private static Object mapIntegerAttribute(InfoMetadata infoMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(infoMetadata)) {
      mappedValue = getAttributeAsInteger(value);
    } else {
      mappedValue = getAttributeAsIntegerList(infoMetadata, value);
    }
    return mappedValue;
  }

  private Object mapNestedAttribute(InfoMetadata infoMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(infoMetadata)) {
      String stringValue = getAttributeAsString(value);
      if (stringValue != null) {
        mappedValue = mapNestedAttributeToInfo(infoMetadata, stringValue);
      } else {
        mappedValue = null;
      }
    } else {
      List<String> stringValues = getAttributeAsStringList(infoMetadata, value);
      if (!stringValues.isEmpty()) {
        mappedValue =
            stringValues.stream()
                .map(stringValue -> mapNestedAttributeToInfo(infoMetadata, stringValue))
                .collect(toList());
      } else {
        mappedValue = emptyList();
      }
    }
    return mappedValue;
  }

  private Collection<Object> mapNestedAttributeToInfo(
      InfoMetadata infoMetadata, String stringValue) {
    String[] tokens = stringValue.split("\\|", -1);
    List<InfoMetadata> nestedInfoMetadataList = infoMetadata.getNestedMetadata();

    Map<String, Object> attributes = new LinkedHashMap<>();
    for (int i = 0; i < tokens.length; ++i) {
      InfoMetadata nestedInfoMetadata = nestedInfoMetadataList.get(i);
      attributes.put(nestedInfoMetadata.getId(), tokens[i]);
    }

    Info nestedInfo = map(nestedInfoMetadataList, attributes);
    return new ArrayList<>(nestedInfo.values());
  }

  private static boolean isSingleValue(InfoMetadata infoMetadata) {
    Number number = infoMetadata.getNumber();
    return number.getType() == Number.Type.NUMBER && number.getCount() == 1;
  }

  private static @Nullable Integer getAttributeAsInteger(Object value) {
    Integer integerValue;
    if (value == null) {
      integerValue = null;
    } else if (value instanceof Integer) {
      integerValue = (Integer) value;
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        integerValue = Integer.parseInt(stringValue);
      } else {
        integerValue = null;
      }
    }
    return integerValue;
  }

  private static List<Integer> getAttributeAsIntegerList(InfoMetadata infoMetadata, Object value) {
    List<Integer> integerValues;
    if (value == null) {
      integerValues = emptyList();
    } else if (value instanceof List) {
      integerValues = mapListAttributeAsIntegerList((List<?>) value);
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        integerValues = mapStringAttributeAsIntegerList(infoMetadata, (String) value);
      } else {
        integerValues = emptyList();
      }
    }
    return integerValues;
  }

  private static List<Integer> mapStringAttributeAsIntegerList(
      InfoMetadata infoMetadata, String value) {
    List<Integer> integerValues;

    if (!value.isEmpty()) {
      String separator = Pattern.quote(String.valueOf(infoMetadata.getNumber().getSeparator()));
      String[] tokens = value.split(separator, -1);
      if (tokens.length == 0) {
        integerValues = emptyList();
      } else {
        integerValues = new ArrayList<>(tokens.length);
        for (String token : tokens) {
          Integer integerValue = getAttributeAsInteger(token);
          integerValues.add(integerValue);
        }
      }
    } else {
      integerValues = emptyList();
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
    if (value == null) {
      doubleValue = null;
    } else if (value instanceof Double) {
      doubleValue = (Double) value;
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        doubleValue = Double.parseDouble(stringValue);
      } else {
        doubleValue = null;
      }
    }
    return doubleValue;
  }

  private static List<Double> getAttributeAsDoubleList(InfoMetadata infoMetadata, Object value) {
    List<Double> doubleValues;
    if (value == null) {
      doubleValues = emptyList();
    } else if (value instanceof List) {
      doubleValues = mapListAttributeAsDoubleList((List<?>) value);
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        doubleValues = mapStringAttributeAsDoubleList(infoMetadata, (String) value);
      } else {
        doubleValues = emptyList();
      }
    }
    return doubleValues;
  }

  private static List<Double> mapStringAttributeAsDoubleList(
      InfoMetadata infoMetadata, String value) {
    List<Double> doubleValues;

    if (!value.isEmpty()) {
      String separator = Pattern.quote(String.valueOf(infoMetadata.getNumber().getSeparator()));
      String[] tokens = value.split(separator, -1);
      if (tokens.length == 0) {
        doubleValues = emptyList();
      } else {
        doubleValues = new ArrayList<>(tokens.length);
        for (String token : tokens) {
          Double doubleValue = getAttributeAsDouble(token);
          doubleValues.add(doubleValue);
        }
      }
    } else {
      doubleValues = emptyList();
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
    if (value == null) {
      stringValue = null;
    } else if (value instanceof List<?>) {
      // workaround for issue where HtsJdk returns a value that is supposed to be a single string
      // value as a list
      // example: ##INFO=<ID=ANN_ALLELE,Number=1,Type=String,...> and ANN_ALLELE=A,A
      stringValue = ((List<?>) value).stream().map(Object::toString).collect(joining(","));
    } else {
      String rawStringValue = (String) value;
      if (!rawStringValue.isEmpty() && !rawStringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        stringValue = rawStringValue;
      } else {
        stringValue = null;
      }
    }
    return stringValue;
  }

  private static List<String> getAttributeAsStringList(InfoMetadata infoMetadata, Object value) {
    List<String> stringValues;
    if (value == null) {
      stringValues = emptyList();
    } else if (value instanceof List) {
      stringValues = mapListAttributeAsStringList((List<?>) value);
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        stringValues = mapStringAttributeAsStringList(infoMetadata, (String) value);
      } else {
        stringValues = emptyList();
      }
    }
    return stringValues;
  }

  private static List<String> mapStringAttributeAsStringList(
      InfoMetadata infoMetadata, String value) {
    List<String> stringValues;

    if (!value.isEmpty()) {
      String separator = Pattern.quote(String.valueOf(infoMetadata.getNumber().getSeparator()));
      String[] tokens = value.split(separator, -1);
      if (tokens.length == 0) {
        stringValues = emptyList();
      } else {
        stringValues = new ArrayList<>(tokens.length);
        for (String token : tokens) {
          String stringValue = getAttributeAsString(token);
          stringValues.add(stringValue);
        }
      }
    } else {
      stringValues = emptyList();
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
