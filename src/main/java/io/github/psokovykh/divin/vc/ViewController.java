package io.github.psokovykh.divin.vc;

import io.github.psokovykh.divin.core.Threadlike;

/**
 * Subclasses of that interface represent simple View-Controllers.
 * They run in separate Thread, so we want them to have Threadlike capabilities
 */
public interface ViewController extends Threadlike {
}
