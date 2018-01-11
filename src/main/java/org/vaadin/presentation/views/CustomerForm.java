/*
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */
package org.vaadin.presentation.views;

import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import java.util.stream.Stream;
import org.vaadin.backend.CustomerService;
import org.vaadin.backend.domain.Customer;
import org.vaadin.backend.domain.CustomerStatus;
import org.vaadin.backend.domain.Gender;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.vaadin.viritin.form.AbstractForm;

/**
 * A UI component built to modify Customer entities. The used superclass
 * provides binding to the entity object and e.g. Save/Cancel buttons by
 * default. You could naturally do this manually as well using buttons, layouts
 * and Binder, but in larger apps, you'll most likely have your own customized
 * super class for your forms.
 * <p>
 * Note, that the advanced bean binding technology in Vaadin is able to take
 * advantage also from Bean Validation annotations that are used also by e.g.
 * JPA implementation. Check out annotations in Customer objects email field and
 * how they automatically reflect to the configuration of related fields in UI.
 * </p>
 */
@Dependent
public class CustomerForm extends AbstractForm<Customer> {

    @Inject
    CustomerService service;

    // Prepare some basic field components that our bound to entity property
    // by naming convetion, you can also use PropertyId annotation
    //*Akbar TextField firstName = new TextField("First name");
    TextField engagementName = new TextField("Engagement Name");
   //*Akbar TextField lastName = new TextField("Last name");
    TextField description = new TextField("Description");
    //*Akbar DateField birthDate = new DateField("Birth day");
    DateField engagementDate = new DateField("Engagement Date");
    // Select to another entity, options are populated in the init method
    //*Akbar NativeSelect<CustomerStatus> status = new NativeSelect("Status");
    NativeSelect<CustomerStatus> status = new NativeSelect("Engagement Type");
    RadioButtonGroup<Gender> gender = new RadioButtonGroup<>("Gender");
    TextField email = new TextField("Email");

    public CustomerForm() {
        super(Customer.class);
    }

    @Override
    protected Component createContent() {
        setStyleName(ValoTheme.LAYOUT_CARD);

        Stream.of(engagementName, description, email).forEach(t -> t.setWidth("100%"));

        return new MVerticalLayout(
                new Header("Edit customer").setHeaderLevel(3),
                new MFormLayout(
                       engagementName,
                        description,
                        email,
                        engagementDate,
                        gender,
                        status
                ).withFullWidth(),
                getToolbar()
        ).withStyleName(ValoTheme.LAYOUT_CARD);
    }

    @PostConstruct
    void init() {
        status.setWidthUndefined();
        status.setItems(CustomerStatus.values());
        gender.setItems(Gender.values());
        gender.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        setSavedHandler(customer -> {
            try {
                // make EJB call to save the entity
                service.saveOrPersist(customer);
                // fire save event to let other UI components know about
                // the change
                saveEvent.fire(customer);
            } catch (EJBException e) {
                /*
                * The Customer object uses optimitic locking with the
                * version field. Notify user the editing didn't succeed.
                 */
                Notification.show("The customer was concurrently edited "
                        + "by someone else. Your changes were discarded.",
                        Notification.Type.ERROR_MESSAGE);
                refrehsEvent.fire(customer);
            }
        });
        setResetHandler(refrehsEvent::fire);
        setDeleteHandler(customer -> {
            service.deleteEntity(getEntity());
            deleteEvent.fire(getEntity());
        });
    }

    @Override
    protected void adjustResetButtonState() {
        // always enabled in this form
        getResetButton().setEnabled(true);
        getDeleteButton().setEnabled(getEntity() != null && getEntity().isPersisted());
    }

    /* "CDI interface" to notify decoupled components. Using traditional API to
     * other componets would probably be easier in small apps, but just
     * demonstrating here how all CDI stuff is available for Vaadin apps.
     */
    @Inject
    @CustomerEvent(CustomerEvent.Type.SAVE)
    javax.enterprise.event.Event<Customer> saveEvent;

    @Inject
    @CustomerEvent(CustomerEvent.Type.REFRESH)
    javax.enterprise.event.Event<Customer> refrehsEvent;

    @Inject
    @CustomerEvent(CustomerEvent.Type.DELETE)
    javax.enterprise.event.Event<Customer> deleteEvent;
}
