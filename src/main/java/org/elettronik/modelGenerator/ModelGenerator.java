package org.elettronik.modelGenerator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModelGenerator {

    private Logger l;

    private Class<?> clazz;
    private boolean idProperty;
    private String pack;
    private List<ModelField> fields;
    private List<String> alreadyParsedClass;
    private String modelName;

    public ModelGenerator(Class<?> clazz, String pack, String modelName) {
        l = LoggerFactory.getLogger(getClass());

        this.alreadyParsedClass = new ArrayList<String>();
        this.clazz = clazz;
        this.fields = new ArrayList<ModelField>();
        if (modelName != null) {
            this.modelName = modelName;
        } else {
            this.modelName = clazz.getSimpleName();
        }
        this.pack = pack;
    }

    public void generate() throws IOException {
        File outFile = getFileName();
        l.info("Output file: {}", outFile.getCanonicalPath());
        PrintWriter pw = new PrintWriter(outFile);

        l.info("Parsing class");

        parseClass();

        //Header
        printHeader(pw);

        writeIdProperty(pw);
        
        pw.println();
        writeFields(pw);

        //Footer
        printFooter(pw);
        IOUtils.closeQuietly(pw);
        l.info("Done");
    }

    private void writeFields(PrintWriter pw) {
        
        pw.println("    fields: [{");
        
        boolean first = true;
        for(ModelField mf: fields) {
            if (first) {
                first = false;
            } else {
                pw.println("    }, {");
            }
            
            pw.println("        name: \"" + mf.getName() + "\",");
            switch (mf.getType()) {
            case  BOOLEAN:
                pw.println("        type: \"boolean\",");
                pw.println("        useNull: true");
                break;
            case DATE:
                pw.println("        type: \"date\",");
                pw.println("        useNull: true,");
                pw.println("        dateFormat: \"Y-m-d H:i:s:u\"");
                break;
            case FLOAT:
                pw.println("        type: \"float\",");
                pw.println("        useNull: true");
                break;
            case INT:
                pw.println("        type: \"int\",");
                pw.println("        useNull: true");
                break;
            case STRING:
                pw.println("        type: \"string\",");
                pw.println("        useNull: true");
                break;
            }
        }
        
        pw.println("    }]");
    }

    private void parseClass() {
        parseClass(null, "");
        
        //Check if there is a field id so create a custom id field
        for (ModelField mf: this.fields) {
            if (mf.getName().equals("id") || 
                    mf.getName().startsWith("id.")) {
                
                idProperty = true;
                break;
            }
        }
    }

    private void parseClass(Class<?> clazz, String prefix) {
        Field[] campiClasse = null;
        if (clazz == null) {
            campiClasse = this.clazz.getDeclaredFields();
        } else {
            campiClasse = clazz.getDeclaredFields();
        }

        for(Field f: campiClasse) {
            //Se il campo è annotato XMLTransient lo salto
            if (f.isAnnotationPresent(XmlTransient.class) || 
                    f.getName().equals("serialVersionUID")) {
                continue;
            }

            //Check for values
            Class<?> type = f.getType();
            if (type.equals(String.class)) {
                
                ModelField mf = new ModelField(prefix + f.getName(), ModelField.Type.STRING);
                fields.add(mf);
            } else if (type.equals(int.class) || 
                    type.equals(long.class) ||
                    type.equals(Integer.class) ||
                    type.equals(Long.class)) {

                ModelField mf = new ModelField(prefix + f.getName(), ModelField.Type.INT);
                fields.add(mf);
            } else if (type.equals(float.class) || 
                    type.equals(double.class) ||
                    type.equals(Float.class) ||
                    type.equals(Double.class) || 
                    type.equals(BigDecimal.class) ) {

                ModelField mf = new ModelField(prefix + f.getName(), ModelField.Type.FLOAT);
                fields.add(mf);
            } else if (type.equals(boolean.class) || 
                    type.equals(Boolean.class)) {

                ModelField mf = new ModelField(prefix + f.getName(), ModelField.Type.BOOLEAN);
                fields.add(mf);
            } else if (type.equals(Date.class) || 
                    type.equals(java.sql.Date.class)) {
                
                ModelField mf = new ModelField(prefix + f.getName(), ModelField.Type.DATE);
                fields.add(mf);
            }else {
                
                //Recursion into sub field
                if (alreadyParsedClass.contains(f.getType().getCanonicalName())) {
                    l.info("Avoid recurse into field {} of type {}", f.getName(), f.getType().getPackage() + f.getType().getName());
                    continue;
                } else {
                    l.info("Recurse into field {} of type {}", f.getName(), f.getType().getPackage() + f.getType().getName());
                    alreadyParsedClass.add(f.getType().getCanonicalName());
                    parseClass(f.getType(), f.getName() + ".");
                }
            }
        }
    }

    private void writeIdProperty(PrintWriter pw) {
        if (idProperty) {
            pw.println();
            pw.println("    idProperty: 'pk',");
        }
    }

    private void printFooter(PrintWriter pw) {
        pw.println("});");
    }

    private void printHeader(PrintWriter pw) {
        pw.println("Ext.define('" + pack + "." + modelName + "', {");
        pw.println("    extend: 'Ext.data.Model',");
    }

    /**
     * Get filename for the model
     * @return
     */
    private File getFileName() {
        return new File(modelName + ".js");
    }

}
