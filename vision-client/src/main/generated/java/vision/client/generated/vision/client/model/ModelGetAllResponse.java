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
import vision.client.generated.vision.client.model.ModelGetAllResponseModels;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * ModelGetAllResponse
 */
@JsonPropertyOrder({
  ModelGetAllResponse.JSON_PROPERTY_MODELS
})
@javax.annotation.processing.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-07-24T17:27:37.459890800+02:00[Europe/Berlin]")
public class ModelGetAllResponse {
  public static final String JSON_PROPERTY_MODELS = "models";
  private List<ModelGetAllResponseModels> models = null;


  public ModelGetAllResponse models(List<ModelGetAllResponseModels> models) {
    this.models = models;
    return this;
  }

  public ModelGetAllResponse addModelsItem(ModelGetAllResponseModels modelsItem) {
    if (this.models == null) {
      this.models = new ArrayList<>();
    }
    this.models.add(modelsItem);
    return this;
  }

   /**
   * Get models
   * @return models
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")
  @JsonProperty(JSON_PROPERTY_MODELS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<ModelGetAllResponseModels> getModels() {
    return models;
  }


  @JsonProperty(JSON_PROPERTY_MODELS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setModels(List<ModelGetAllResponseModels> models) {
    this.models = models;
  }


  /**
   * Return true if this ModelGetAllResponse object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelGetAllResponse modelGetAllResponse = (ModelGetAllResponse) o;
    return Objects.equals(this.models, modelGetAllResponse.models);
  }

  @Override
  public int hashCode() {
    return Objects.hash(models);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelGetAllResponse {\n");
    sb.append("    models: ").append(toIndentedString(models)).append("\n");
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
