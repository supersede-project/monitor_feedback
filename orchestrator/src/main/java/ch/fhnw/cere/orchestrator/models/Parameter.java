package ch.fhnw.cere.orchestrator.models;


import ch.fhnw.cere.orchestrator.serialization.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.*;


@Entity
@JsonSerialize(using = ParameterSerializer.class)
@JsonDeserialize(using = ParameterDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="`key`")
    private String key;
    @JsonSerialize(using = ParameterValueSerializer.class)
    private String value;
    private Date createdAt;
    private Date updatedAt;
    private String language;
    @Column(name="`order`")
    private int order;

    @ManyToOne
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="parent_parameter_id")
    private Parameter parentParameter;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy="parentParameter", cascade={CascadeType.REMOVE, CascadeType.PERSIST})
    private List<Parameter> parameters;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="general_configuration_id")
    private GeneralConfiguration generalConfiguration;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="mechanism_id")
    private Mechanism mechanism;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    public Parameter() {

    }

    public Parameter(String key, String value, Date createdAt, Date updatedAt, String language, Parameter parentParameter, List<Parameter> parameters, GeneralConfiguration generalConfiguration, Mechanism mechanism) {
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.parentParameter = parentParameter;
        this.parameters = parameters;
        this.generalConfiguration = generalConfiguration;
        this.mechanism = mechanism;
    }

    public Parameter(long id, String key, String value, Date createdAt, Date updatedAt, String language, Parameter parentParameter, List<Parameter> parameters, GeneralConfiguration generalConfiguration, Mechanism mechanism) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.parentParameter = parentParameter;
        this.parameters = parameters;
        this.generalConfiguration = generalConfiguration;
        this.mechanism = mechanism;
    }

    public Parameter(long id, String key, String value, Date createdAt, Date updatedAt, String language, Parameter parentParameter, List<Parameter> parameters) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.parentParameter = parentParameter;
        this.parameters = parameters;
        this.generalConfiguration = generalConfiguration;
        this.mechanism = mechanism;
    }

    public Parameter(String key, String value, Date createdAt, Date updatedAt, String language, Parameter parentParameter, List<Parameter> parameters) {
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.parentParameter = parentParameter;
        this.parameters = parameters;
        this.generalConfiguration = generalConfiguration;
        this.mechanism = mechanism;
    }

    public Parameter(String key, String value, Date createdAt, Date updatedAt, String language, Parameter parentParameter, GeneralConfiguration generalConfiguration, Mechanism mechanism) {
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.parentParameter = parentParameter;
        this.generalConfiguration = generalConfiguration;
        this.mechanism = mechanism;
    }

    public Parameter(String key, String value, String language, Parameter parentParameter, List<Parameter> parameters) {
        this.key = key;
        this.value = value;
        this.language = language;
        this.parentParameter = parentParameter;
        this.parameters = parameters;
    }

    public Parameter(String key, String value) {
        this.key = key;
        this.value = value;
        this.language = "en";
    }

    public Parameter(String key, String value, String language, GeneralConfiguration generalConfiguration) {
        this.key = key;
        this.value = value;
        this.language = language;
        this.generalConfiguration = generalConfiguration;
    }

    public Parameter(long id, String key, String value, String language, Parameter parentParameter, List<Parameter> parameters) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.language = language;
        this.parentParameter = parentParameter;
        this.parameters = parameters;
    }

    public Parameter(String key, String value, Date createdAt, Date updatedAt, String language, int order, Parameter parentParameter, List<Parameter> parameters, GeneralConfiguration generalConfiguration, Mechanism mechanism) {
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.order = order;
        this.parentParameter = parentParameter;
        this.parameters = parameters;
        this.generalConfiguration = generalConfiguration;
        this.mechanism = mechanism;
    }

    @Override
    public String toString() {
        return String.format(
                "Parameter[id=%d, key='%s', value='%s', language='%s', order='%d']",
                id, key, value, language, order);
    }

    List<Parameter> parametersByLanguage(String language, String fallbackLanguage) {
        if(this.getParameters() == null) {
            return null;
        }
        Map<String, Parameter> keyValuePairs = new HashMap<>();
        for(Parameter parameter : this.getParameters()) {
            if(parameter.getParameters() != null && parameter.getParameters().size() > 0) {
                parameter.setParameters(parameter.parametersByLanguage(language, fallbackLanguage));
            }

            if(keyValuePairs.containsKey(parameter.getKey())) {
                if(parameter.getLanguage().equals(language)) {
                    keyValuePairs.put(parameter.getKey(), parameter);
                } else if (!keyValuePairs.get(parameter.getKey()).getLanguage().equals(language) && parameter.getLanguage().equals(fallbackLanguage)) {
                    keyValuePairs.put(parameter.getKey(), parameter);
                }
            } else if(parameter.getLanguage().equals(language) || parameter.getLanguage().equals(fallbackLanguage)) {
                keyValuePairs.put(parameter.getKey(), parameter);
            }
        }
        List<Parameter> parameters = new ArrayList<>(keyValuePairs.values());
        parameters.sort(Comparator.comparingInt(Parameter::getOrder));
        return parameters;
    }

    void filterByLanguage(String language, String fallbackLanguage) {
        if(this.getParameters() == null) {
            return;
        }
        for(Parameter parameter : this.getParameters()) {
            parameter.filterByLanguage(language, fallbackLanguage);
        }
        this.setParameters(this.parametersByLanguage(language, fallbackLanguage));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Parameter getParentParameter() {
        return parentParameter;
    }

    public void setParentParameter(Parameter parentParameter) {
        this.parentParameter = parentParameter;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Mechanism getMechanism() {
        return mechanism;
    }

    public void setMechanism(Mechanism mechanism) {
        this.mechanism = mechanism;
    }

    public GeneralConfiguration getGeneralConfiguration() {
        return generalConfiguration;
    }

    public void setGeneralConfiguration(GeneralConfiguration generalConfiguration) {
        this.generalConfiguration = generalConfiguration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}




