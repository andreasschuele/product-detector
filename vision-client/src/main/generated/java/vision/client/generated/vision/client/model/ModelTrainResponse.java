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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * ModelTrainResponse
 */
@JsonPropertyOrder({
  ModelTrainResponse.JSON_PROPERTY_TRAIN_ID
})
@javax.annotation.processing.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-07-24T17:27:37.459890800+02:00[Europe/Berlin]")
public class ModelTrainResponse {
  public static final String JSON_PROPERTY_TRAIN_ID = "trainId";
  private String trainId;


  public ModelTrainResponse trainId(String trainId) {
    this.trainId = trainId;
    return this;
  }

   /**
   * Get trainId
   * @return trainId
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")
  @JsonProperty(JSON_PROPERTY_TRAIN_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getTrainId() {
    return trainId;
  }


  @JsonProperty(JSON_PROPERTY_TRAIN_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTrainId(String trainId) {
    this.trainId = trainId;
  }


  /**
   * Return true if this ModelTrainResponse object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelTrainResponse modelTrainResponse = (ModelTrainResponse) o;
    return Objects.equals(this.trainId, modelTrainResponse.trainId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trainId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelTrainResponse {\n");
    sb.append("    trainId: ").append(toIndentedString(trainId)).append("\n");
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

