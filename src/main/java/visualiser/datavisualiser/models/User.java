package visualiser.datavisualiser.models;

import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.GraphDetector.VisSchemaPattern;
import visualiser.datavisualiser.models.RelationalModel.ERModel;
import visualiser.datavisualiser.models.RelationalModel.Keys.Attribute;
import visualiser.datavisualiser.models.RelationalModel.Keys.PrimaryAttribute;
import visualiser.datavisualiser.models.RelationalModel.Relationships.Relationship;

import java.util.List;

public class User {

    private ERModel rm;
    private GraphDetector gd;
    private VisSchemaPattern v;
    private Relationship r;
    private PrimaryAttribute k1;
    private PrimaryAttribute k2;
    private List<Attribute> as;
    private GraphPlan plan;

    public ERModel getRelationalModel() {
        return rm;
    }

    public void setRelationalModel(ERModel rm) {
        this.rm = rm;
    }

    public GraphDetector getGraphDetector() {
        return gd;
    }

    public void setGraphDetector(GraphDetector gd) {
        this.gd = gd;
    }

    public VisSchemaPattern getVisSchemaPattern() {
        return v;
    }

    public void setVisSchemaPattern(VisSchemaPattern v) {
        this.v = v;
    }

    public Relationship getRelationship() {
        return r;
    }

    public void setRelationship(Relationship r) {
        this.r = r;
    }

    public PrimaryAttribute getK1() {
        return k1;
    }

    public void setK1(PrimaryAttribute k1) {
        this.k1 = k1;
    }

    public PrimaryAttribute getK2() {
        return k2;
    }

    public void setK2(PrimaryAttribute k2) {
        this.k2 = k2;
    }

    public List<Attribute> getAttributes() {
        return as;
    }

    public void setAttributes(List<Attribute> as) {
        this.as = as;
    }

    public GraphPlan getPlan() {
        return plan;
    }

    public void setPlan(GraphPlan plan) {
        this.plan = plan;
    }
}
