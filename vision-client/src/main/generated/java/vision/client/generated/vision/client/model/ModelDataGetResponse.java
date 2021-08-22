/*
 * Vision API
 * The Vision component API.
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package vision.client.generated.vision.client.model;

import java.util.Objects;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import vision.client.generated.vision.client.model.Image;
import vision.client.generated.vision.client.model.ObjectInData;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * ModelDataGetResponse
 */
@JsonPropertyOrder({
  ModelDataGetResponse.JSON_PROPERTY_IMAGE,
  ModelDataGetResponse.JSON_PROPERTY_OBJECTS
})
@javax.annotation.processing.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-07-24T17:27:37.459890800+02:00[Europe/Berlin]")
public class ModelDataGetResponse {
  public static final String JSON_PROPERTY_IMAGE = "image";
  private Image image;

  public static final String JSON_PROPERTY_OBJECTS = "objects";
  private List<ObjectInData> objects = null;


  public ModelDataGetResponse image(Image image) {
    this.image = image;
    return this;
  }

   /**
   * Get image
   * @return image
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")
  @JsonProperty(JSON_PROPERTY_IMAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Image getImage() {
    return image;
  }


  @JsonProperty(JSON_PROPERTY_IMAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setImage(Image image) {
    this.image = image;
  }


  public ModelDataGetResponse objects(List<ObjectInData> objects) {
    this.objects = objects;
    return this;
  }

  public ModelDataGetResponse addObjectsItem(ObjectInData objectsItem) {
    if (this.objects == null) {
      this.objects = new ArrayList<>();
    }
    this.objects.add(objectsItem);
    return this;
  }

   /**
   * Get objects
   * @return objects
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")
  @JsonProperty(JSON_PROPERTY_OBJECTS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<ObjectInData> getObjects() {
    return objects;
  }


  @JsonProperty(JSON_PROPERTY_OBJECTS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setObjects(List<ObjectInData> objects) {
    this.objects = objects;
  }


  /**
   * Return true if this ModelDataGetResponse object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelDataGetResponse modelDataGetResponse = (ModelDataGetResponse) o;
    return Objects.equals(this.image, modelDataGetResponse.image) &&
        Objects.equals(this.objects, modelDataGetResponse.objects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image, objects);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelDataGetResponse {\n");
    sb.append("    image: ").append(toIndentedString(image)).append("\n");
    sb.append("    objects: ").append(toIndentedString(objects)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

