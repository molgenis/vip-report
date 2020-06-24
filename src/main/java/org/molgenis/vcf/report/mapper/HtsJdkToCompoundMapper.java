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
import org.molgenis.vcf.report.model.Compound;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.springframework.lang.Nullable;

public abstract class HtsJdkToCompoundMapper<T extends Compound> {

  public abstract T map(
      List<CompoundMetadata<T>> compoundMetadataList, Map<String, Object> attributes);

  protected Object mapAttribute(CompoundMetadata<T> compoundMetadata, Object value) {
    Object mappedValue;
    if (compoundMetadata != null) {
      @NonNull CompoundMetadata.Type type = compoundMetadata.getType();
      switch (type) {
        case CHARACTER:
          mappedValue = mapCharacterAttribute(compoundMetadata, value);
          break;
        case INTEGER:
          mappedValue = mapIntegerAttribute(compoundMetadata, value);
          break;
        case FLAG:
          mappedValue = mapFlagAttribute(value);
          break;
        case FLOAT:
          mappedValue = mapFloatAttribute(compoundMetadata, value);
          break;
        case STRING:
          mappedValue = mapStringAttribute(compoundMetadata, value);
          break;
        case NESTED:
          mappedValue = mapNestedAttribute(compoundMetadata, value);
          break;
        default:
          throw new UnexpectedEnumException(type);
      }
    } else {
      mappedValue = value;
    }
    return mappedValue;
  }

  private Object mapCharacterAttribute(CompoundMetadata<T> infoMetadata, Object value) {
    return mapStringAttribute(infoMetadata, value);
  }

  private Object mapStringAttribute(CompoundMetadata<T> infoMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(infoMetadata)) {
      mappedValue = getAttributeAsString(value);
    } else {
      mappedValue = getAttributeAsStringList(infoMetadata, value);
    }
    return mappedValue;
  }

  private Object mapFloatAttribute(CompoundMetadata<T> compoundMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(compoundMetadata)) {
      mappedValue = getAttributeAsDouble(value);
    } else {
      mappedValue = getAttributeAsDoubleList(compoundMetadata, value);
    }
    return mappedValue;
  }

  private Object mapIntegerAttribute(CompoundMetadata<T> compoundMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(compoundMetadata)) {
      mappedValue = getAttributeAsInteger(value);
    } else {
      mappedValue = getAttributeAsIntegerList(compoundMetadata, value);
    }
    return mappedValue;
  }

  private Object mapNestedAttribute(CompoundMetadata<T> compoundMetadata, Object value) {
    Object mappedValue;
    if (isSingleValue(compoundMetadata)) {
      String stringValue = getAttributeAsString(value);
      if (stringValue != null) {
        mappedValue = mapNestedAttributeToInfo(compoundMetadata, stringValue);
      } else {
        mappedValue = null;
      }
    } else {
      List<String> stringValues = getAttributeAsStringList(compoundMetadata, value);
      if (!stringValues.isEmpty()) {
        mappedValue =
            stringValues.stream()
                .map(stringValue -> mapNestedAttributeToInfo(compoundMetadata, stringValue))
                .collect(toList());
      } else {
        mappedValue = emptyList();
      }
    }
    return mappedValue;
  }

  private Collection<Object> mapNestedAttributeToInfo(
      CompoundMetadata<T> compoundMetadata, String stringValue) {
    String[] tokens = stringValue.split("\\|", -1);
    List<CompoundMetadata<T>> nestedInfoMetadataList = compoundMetadata.getNestedMetadata();

    Map<String, Object> attributes = new LinkedHashMap<>();
    for (int i = 0; i < tokens.length; ++i) {
      CompoundMetadata<T> nestedCompoundMetadata = nestedInfoMetadataList.get(i);
      attributes.put(nestedCompoundMetadata.getId(), tokens[i]);
    }

    T nestedCompound = map(nestedInfoMetadataList, attributes);
    return new ArrayList<>(nestedCompound.values());
  }

  private boolean isSingleValue(CompoundMetadata<T> compoundMetadata) {
    Number number = compoundMetadata.getNumber();
    return number.getType() == Number.Type.NUMBER && number.getCount() == 1;
  }

  private @Nullable
  Integer getAttributeAsInteger(Object value) {
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

  private List<Integer> getAttributeAsIntegerList(
      CompoundMetadata<T> compoundMetadata, Object value) {
    List<Integer> integerValues;
    if (value == null) {
      integerValues = emptyList();
    } else if (value instanceof List) {
      integerValues = mapListAttributeAsIntegerList((List<?>) value);
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        integerValues = mapStringAttributeAsIntegerList(compoundMetadata, (String) value);
      } else {
        integerValues = emptyList();
      }
    }
    return integerValues;
  }

  private List<Integer> mapStringAttributeAsIntegerList(
      CompoundMetadata<T> compoundMetadata, String value) {
    List<Integer> integerValues;

    if (!value.isEmpty()) {
      String separator = Pattern.quote(String.valueOf(compoundMetadata.getNumber().getSeparator()));
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

  private List<Integer> mapListAttributeAsIntegerList(List<?> values) {
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

  private @Nullable
  Double getAttributeAsDouble(Object value) {
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

  private List<Double> getAttributeAsDoubleList(
      CompoundMetadata<T> compoundMetadata, Object value) {
    List<Double> doubleValues;
    if (value == null) {
      doubleValues = emptyList();
    } else if (value instanceof List) {
      doubleValues = mapListAttributeAsDoubleList((List<?>) value);
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        doubleValues = mapStringAttributeAsDoubleList(compoundMetadata, (String) value);
      } else {
        doubleValues = emptyList();
      }
    }
    return doubleValues;
  }

  private List<Double> mapStringAttributeAsDoubleList(
      CompoundMetadata<T> compoundMetadata, String value) {
    List<Double> doubleValues;

    if (!value.isEmpty()) {
      String separator = Pattern.quote(String.valueOf(compoundMetadata.getNumber().getSeparator()));
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

  private List<Double> mapListAttributeAsDoubleList(List<?> values) {
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

  private static @Nullable
  String getAttributeAsString(Object value) {
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

  private List<String> getAttributeAsStringList(
      CompoundMetadata<T> compoundMetadata, Object value) {
    List<String> stringValues;
    if (value == null) {
      stringValues = emptyList();
    } else if (value instanceof List) {
      stringValues = mapListAttributeAsStringList((List<?>) value);
    } else {
      String stringValue = (String) value;
      if (!stringValue.isEmpty() && !stringValue.equals(VCFConstants.MISSING_VALUE_v4)) {
        stringValues = mapStringAttributeAsStringList(compoundMetadata, (String) value);
      } else {
        stringValues = emptyList();
      }
    }
    return stringValues;
  }

  private List<String> mapStringAttributeAsStringList(
      CompoundMetadata<T> compoundMetadata, String value) {
    List<String> stringValues;

    if (!value.isEmpty()) {
      String separator = Pattern.quote(String.valueOf(compoundMetadata.getNumber().getSeparator()));
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
