package org.elettronik.modelGenerator;

public class ModelField {
    
    enum Type {
        BOOLEAN,
        DATE,
        FLOAT,
        INT,
        STRING
    };
    
    private String name;
    private Type type;

    
    public ModelField() {
    }
    
    @Override
    public String toString() {
        return "ModelField [name=" + name + ", type=" + type + "]";
    }

    public ModelField(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
