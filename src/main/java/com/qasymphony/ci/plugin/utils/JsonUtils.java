package com.qasymphony.ci.plugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author trongle
 * @version 10/21/2015 2:24 PM trongle $
 * @since 1.0
 */
public class JsonUtils {
  private static final Logger LOG = Logger.getLogger(JsonUtils.class.getName());
  public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
  private static final List<String> UTCS = Arrays.asList(
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSSX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX",
    "yyyy-MM-dd'T'HH:mm:ss.sX",
    "yyyy-MM-dd'T'HH:mm:ssX",
    "yyyy-MM-dd'T'HH:mm:ss");

  /**
   * Use for JSON
   */
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    // mapper
    mapper.setDateFormat(new SimpleDateFormat(UTC_DATE_FORMAT));
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private JsonUtils() {

  }

  /**
   * Create new ObjectNode
   *
   * @return {@link ObjectNode}
   */
  public static ObjectNode newNode() {
    return mapper.createObjectNode();
  }

  /**
   * @return current datetime
   */
  public static String getCurrentDateString() {
    return new SimpleDateFormat(UTC_DATE_FORMAT).format(new Date());
  }

  /**
   * Get text by field in node
   *
   * @param node  the {@link JsonNode}
   * @param field the field that use to get in {@link JsonNode}
   * @return text in node if field exists in node, otherwise return empty string
   */
  public static String getText(JsonNode node, String field) {
    if (null == node || node.get(field) == null)
      return "";
    return node.get(field).asText();
  }

  /**
   * Get int value from JsonNode
   *
   * @param node  node
   * @param field field
   * @return int
   */
  public static int getInt(JsonNode node, String field) {
    if (null == node || node.get(field) == null)
      return 0;
    return node.get(field).asInt();
  }

  /**
   * parse a string value to JsonNode
   *
   * @param body body
   * @return {@link JsonNode}
   */
  public static JsonNode readTree(String body) {
    JsonNode node = null;
    try {
      node = parseTree(body);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "readTree: Cannot readTree from body string.", e);
    }
    return node;
  }

  public static JsonNode parseTree(String body) throws IOException {
    JsonNode node = null;
    if (StringUtils.isEmpty(body))
      return node;
    return mapper.readTree(body);
  }

  /**
   * Get long from json node
   *
   * @param node  node
   * @param field field
   * @return long value
   */
  public static Long getLong(JsonNode node, String field) {
    if (null == node || node.get(field) == null)
      return 0L;
    return node.get(field).asLong(0);
  }

  /**
   * Get instance of valueType from JSON data
   *
   * @param body      JSON string
   * @param valueType class type to cast
   * @param <T>       T
   * @return instance of class valueType
   */
  public static <T> T fromJson(String body, Class<T> valueType) {
    try {
      return parseJson(body, valueType);
    } catch (IOException e) {
      LOG.log(Level.WARNING, String.format("Cannot mapping from JSON to %s", valueType), e);
      return null;
    }
  }

  /**
   * @param body      bodu
   * @param valueType valueType
   * @param <T>       T
   * @return T
   * @throws IOException IOException
   */
  public static <T> T parseJson(String body, Class<T> valueType) throws IOException {
    if (StringUtils.isEmpty(body))
      return null;
    return mapper.readValue(body, valueType);
  }

  public static <T> T fromJson(String body, TypeReference<T> type) {
    try {
      if (StringUtils.isEmpty(body))
        return null;
      return mapper.readValue(body, type);
    } catch (IOException e) {
      LOG.log(Level.WARNING, String.format("Cannot mapping from JSON to %s", type), e);
      return null;
    }
  }

  /**
   * Get JsonNode from object append field extraData to JsonNode
   *
   * @param data data
   * @return JsonNode, if data is a instance of POJO. Otherwise return empty string
   */
  public static JsonNode toJsonNode(Object data) {
    if (null == data)
      return newNode();

    ObjectNode node = null;

    try {
      // convert data to ObjectNode
      node = mapper.valueToTree(data);
    } catch (IllegalArgumentException e) {
      return newNode();
    }
    return node;
  }

  /**
   * serial object to JSON string
   *
   * @param data data
   * @return json string
   */
  public static String toJson(Object data) {
    if (null == data)
      return "";
    try {
      return mapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      LOG.log(Level.WARNING, "Cannot serial object to JSON.", e);
      return "";
    }
  }

  /**
   * Parse timestamp to date
   *
   * @param timestamp in utc format
   * @return
   */
  public static Date parseTimestamp(String timestamp) {
    if (StringUtils.isEmpty(timestamp)) {
      return null;
    }
    try {
      //try to attempts parse timestamp as long
      Long time = Long.parseLong(timestamp);
      if (time > 0) {
        return new Date(time);
      }
    } catch (Exception e) {
      //fine to ignore
    }

    for (String format : UTCS) {
      try {
        return new SimpleDateFormat(format).parse(timestamp);
      } catch (Exception e) {
        LOG.log(Level.WARNING, "Failed to attempts parse suite timestamp:" + timestamp + ", format:" + format + ",err: " + e.getMessage());
      }
    }
    return null;
  }
}
