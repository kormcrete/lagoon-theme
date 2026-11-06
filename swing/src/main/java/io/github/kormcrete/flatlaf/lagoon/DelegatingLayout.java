package io.github.kormcrete.flatlaf.lagoon;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/** A layout that does what another does; subclasses adjust its result. */
class DelegatingLayout implements LayoutManager {

    private final LayoutManager delegate;

    DelegatingLayout(LayoutManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        delegate.addLayoutComponent(name, comp);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        delegate.removeLayoutComponent(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return delegate.preferredLayoutSize(parent);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return delegate.minimumLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        delegate.layoutContainer(parent);
    }
}
