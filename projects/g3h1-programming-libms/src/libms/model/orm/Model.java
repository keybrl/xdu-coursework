package libms.model.orm;

public abstract class Model {
    static class NullPrimaryKey extends IllegalArgumentException {
        NullPrimaryKey(String errMessage) {
            super(errMessage);
        }
    }
    static class UnexpectedtNull extends IllegalArgumentException {
        UnexpectedtNull(String errMessage) {
            super(errMessage);
        }
    }
    static class UnexpectedValue extends IllegalArgumentException {
        UnexpectedValue(String errMessage) {
            super(errMessage);
        }
    }
}
