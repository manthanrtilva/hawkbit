/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleAddUpdateWindow;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleBorderWithIcon;
import org.eclipse.hawkbit.ui.layouts.AbstractCreateUpdateTagLayout;
import org.eclipse.hawkbit.ui.management.targettable.TargetAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPUIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Superclass for pop-up-windows including a minimize and close icon in the
 * upper right corner and a save and cancel button at the bottom.
 */
public class CommonDialogWindow extends Window implements Serializable {

    private static final long serialVersionUID = -1321949234316858703L;

    private final VerticalLayout mainLayout = new VerticalLayout();

    private final String caption;

    private final Component content;

    private final String helpLink;

    private Button saveButton;

    private Button cancelButton;

    private HorizontalLayout buttonsLayout;

    protected ValueChangeListener buttonEnableListener;

    private final ClickListener saveButtonClickListener;

    private final ClickListener cancelButtonClickListener;

    private Map<String, Boolean> requiredFields;

    private final Map<String, Boolean> editedFields;

    @Autowired
    private I18N i18n;

    /**
     * Constructor.
     * 
     * @param caption
     *            the caption
     * @param content
     *            the content
     * @param helpLink
     *            the helpLinks
     * @param saveButtonClickListener
     *            the saveButtonClickListener
     * @param cancelButtonClickListener
     *            the cancelButtonClickListener
     */
    public CommonDialogWindow(final String caption, final Component content, final String helpLink,
            final ClickListener saveButtonClickListener, final ClickListener cancelButtonClickListener,
            final Map<String, Boolean> requiredFields, final Map<String, Boolean> editedFields) {
        checkNotNull(saveButtonClickListener);
        checkNotNull(cancelButtonClickListener);
        this.caption = caption;
        this.content = content;
        this.helpLink = helpLink;
        this.saveButtonClickListener = saveButtonClickListener;
        this.cancelButtonClickListener = cancelButtonClickListener;
        this.requiredFields = requiredFields;
        this.editedFields = editedFields;

        init();
    }

    public void checkMandatoryTextField(final TextChangeEvent event, final AbstractTextField textfield) {

        if (StringUtils.isNotBlank(event.getText())) {
            if (StringUtils.isNotBlank(textfield.getCaption())) {
                requiredFields.put(textfield.getCaption(), Boolean.TRUE);
            }
            getRequiredFields().put(textfield.getId(), Boolean.TRUE);
        } else {
            if (StringUtils.isNotBlank(textfield.getCaption())) {
                requiredFields.put(textfield.getCaption(), Boolean.FALSE);
            }
            requiredFields.put(textfield.getId(), Boolean.FALSE);
        }
        checkMandatoryFieldsFilled();
    }

    public void checkMandatoryComboBox(final ValueChangeEvent event, final AbstractSelect select) {

        if (event.getProperty().getValue() != null) {
            if (StringUtils.isNotBlank(select.getCaption())) {
                requiredFields.put(select.getCaption(), Boolean.TRUE);
            }
            requiredFields.put(select.getId(), Boolean.TRUE);
        } else {
            if (StringUtils.isNotBlank(select.getCaption())) {
                requiredFields.put(select.getCaption(), Boolean.FALSE);
            }
            requiredFields.put(select.getId(), Boolean.FALSE);
        }
        checkMandatoryFieldsFilled();
    }

    /**
     * Checks the mandatory fields in the pop-up-window content. If all
     * mandatory fields are filled the save button is enabled. Otherwise the
     * save button is disabled.
     */
    private void checkMandatoryFieldsFilled() {

        for (final Map.Entry<String, Boolean> entry : requiredFields.entrySet()) {
            if (entry.getValue() == null || entry.getValue().equals(Boolean.FALSE)) {
                saveButton.setEnabled(false);
                return;
            }
        }
        saveButton.setEnabled(true);
    }

    private void checkExistsChanges() {

        if (editedFields == null) {
            return;
        }
        for (final Map.Entry<String, Boolean> entry : editedFields.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(Boolean.TRUE)) {
                saveButton.setEnabled(true);
                return;
            }
        }
        saveButton.setEnabled(false);
    }

    public void updateRequiredFields(final String fieldId, final Boolean filled) {

        requiredFields.put(fieldId, filled);
        checkMandatoryFieldsFilled();
    }

    public void checkChanges(final String fieldName, final String newText, final String oldText) {

        if ((StringUtils.isNotBlank(newText) && !newText.equals(oldText))
                || (StringUtils.isNotBlank(oldText) && !oldText.equals(newText))) {
            editedFields.put(fieldName, Boolean.TRUE);
        } else {
            editedFields.put(fieldName, Boolean.FALSE);
        }
        checkExistsChanges();
    }

    public void checkColorChange(final String fieldName, final Color newColor, final Color oldColor) {

        if (newColor.equals(oldColor)) {
            editedFields.put(fieldName, Boolean.FALSE);
        } else {
            editedFields.put(fieldName, Boolean.TRUE);
        }
        checkExistsChanges();
    }

    public void resetMandatoryAndEditedFields() {
        resetFields(requiredFields);
        resetFields(editedFields);
    }

    private void resetFields(final Map<String, Boolean> fields) {
        // Reset mandatory fields are filled marker / fields are edited marker
        if (fields != null) {
            for (final Map.Entry<String, Boolean> entry : fields.entrySet()) {
                entry.setValue(null);
            }
        }
    }

    private final void init() {

        if (content instanceof AbstractOrderedLayout) {
            ((AbstractOrderedLayout) content).setSpacing(true);
            ((AbstractOrderedLayout) content).setMargin(true);
        }
        if (content instanceof GridLayout) {
            addStyleName("marginTop");
        }

        if (null != content) {
            mainLayout.addComponent(content);
        }

        createMandatoryLabel();

        final HorizontalLayout buttonLayout = createActionButtonsLayout();
        mainLayout.addComponent(buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.TOP_CENTER);

        setCaption(caption);
        setContent(mainLayout);
        setResizable(false);
        center();
        setModal(true);
        addStyleName("fontsize");
    }

    private HorizontalLayout createActionButtonsLayout() {

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.setSpacing(true);

        createSaveButton();

        createCancelButton();
        buttonsLayout.addStyleName("actionButtonsMargin");

        addHelpLink();

        return buttonsLayout;
    }

    private void createMandatoryLabel() {

        if (existsMandatoryFieldsInWindowContent()) {
            // final Label madatoryLabel = new
            // Label(i18n.get("label.mandatory.field"));
            final Label mandatoryLabel = new Label("* Mandatory Field");
            mandatoryLabel.addStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR + " " + ValoTheme.LABEL_TINY);

            if (content instanceof TargetAddUpdateWindowLayout) {
                ((TargetAddUpdateWindowLayout) content).getFormLayout().addComponent(mandatoryLabel);
            } else if (content instanceof SoftwareModuleAddUpdateWindow) {
                ((SoftwareModuleAddUpdateWindow) content).getFormLayout().addComponent(mandatoryLabel);
            } else if (content instanceof AbstractCreateUpdateTagLayout) {
                ((AbstractCreateUpdateTagLayout) content).getMainLayout().addComponent(mandatoryLabel);
            }

            mainLayout.addComponent(mandatoryLabel);
        }
    }

    private void createCancelButton() {
        cancelButton = SPUIComponentProvider.getButton(SPUIComponentIdProvider.CANCEL_BUTTON, "Cancel", "", "", true,
                FontAwesome.TIMES, SPUIButtonStyleBorderWithIcon.class);
        cancelButton.setSizeUndefined();
        cancelButton.addStyleName("default-color");
        cancelButton.addClickListener(cancelButtonClickListener);

        buttonsLayout.addComponent(cancelButton);
        buttonsLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
        buttonsLayout.setExpandRatio(cancelButton, 1.0F);
    }

    private void createSaveButton() {
        saveButton = SPUIComponentProvider.getButton(SPUIComponentIdProvider.SAVE_BUTTON, "Save", "", "", true,
                FontAwesome.SAVE, SPUIButtonStyleBorderWithIcon.class);
        saveButton.setSizeUndefined();
        saveButton.addStyleName("default-color");
        saveButton.addClickListener(saveButtonClickListener);
        saveButton.setEnabled(!existsMandatoryFieldsInWindowContent());
        buttonsLayout.addComponent(saveButton);
        buttonsLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
        buttonsLayout.setExpandRatio(saveButton, 1.0F);
    }

    private boolean existsMandatoryFieldsInWindowContent() {

        if (requiredFields != null && requiredFields.size() > 0) {
            return true;
        }
        return false;
    }

    private void addHelpLink() {

        if (StringUtils.isEmpty(helpLink)) {
            return;
        }
        final Link helpLinkComponent = SPUIComponentProvider.getHelpLink(helpLink);
        buttonsLayout.addComponent(helpLinkComponent);
        buttonsLayout.setComponentAlignment(helpLinkComponent, Alignment.MIDDLE_RIGHT);
    }

    public void setSaveButtonEnabled(final boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    public void setCancelButtonEnabled(final boolean enabled) {
        cancelButton.setEnabled(enabled);
    }

    public HorizontalLayout getButtonsLayout() {
        return buttonsLayout;
    }

    public Map<String, Boolean> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(final Map<String, Boolean> requiredFields) {
        this.requiredFields = requiredFields;
    }

}
