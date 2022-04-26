package io.github.theepicblock.polymc.api.block;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;

public interface PolyWizardProvider {
    boolean hasPolyWizard();
    Wizard createPolyWizard(WizardInfo info);
}
