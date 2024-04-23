package io.x4ala1c.tsid;

abstract class AbstractFluent<S extends AbstractFluent<S>> {

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }
}
