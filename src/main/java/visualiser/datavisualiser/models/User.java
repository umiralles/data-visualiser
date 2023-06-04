package visualiser.datavisualiser.models;

import visualiser.datavisualiser.models.ERModel.ERModel;
import visualiser.datavisualiser.models.ERModel.Relationships.Relationship;
import visualiser.datavisualiser.models.GraphDetector.GraphDetector;
import visualiser.datavisualiser.models.GraphDetector.GraphPlans.GraphPlan;
import visualiser.datavisualiser.models.GraphDetector.VisSchemaPattern;

public class User {

    private ERModel rm;
    private GraphDetector gd;
    private VisSchemaPattern v;
    private Relationship r;
    private GraphPlan plan;

    public ERModel getERModel() {
        return rm;
    }

    public void setERModel(ERModel rm) {
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

    public GraphPlan getPlan() {
        return plan;
    }

    public void setPlan(GraphPlan plan) {
        this.plan = plan;
    }
}
