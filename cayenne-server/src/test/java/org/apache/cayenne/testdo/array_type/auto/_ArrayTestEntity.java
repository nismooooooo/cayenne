package org.apache.cayenne.testdo.array_type.auto;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _ArrayTestEntity was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _ArrayTestEntity extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "ID";

    public static final Property<Double[]> DOUBLE_ARRAY = Property.create("doubleArray", Double[].class);

    public void setDoubleArray(Double[] doubleArray) {
        writeProperty("doubleArray", doubleArray);
    }
    public Double[] getDoubleArray() {
        return (Double[])readProperty("doubleArray");
    }

}
