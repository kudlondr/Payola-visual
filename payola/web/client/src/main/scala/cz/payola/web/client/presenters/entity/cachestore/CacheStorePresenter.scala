package cz.payola.web.client.presenters.entity.cachestore

import cz.payola.common.entities.EmbeddingDescription
import cz.payola.domain.entities
import cz.payola.web.client.Presenter
import cz.payola.web.client.models.Model
import cz.payola.web.client.views.bootstrap.InputControl
import cz.payola.web.client.views.elements.form.fields._
import cz.payola.web.shared.Payola
import cz.payola.web.shared.managers.EmbeddingDescriptionManager
import s2js.adapters.html

import cz.payola.web.client.events.EventArgs
import cz.payola.web.client.views.elements.form.Field
import cz.payola.web.shared.managers.CustomizationManager
import cz.payola.web.client.views.bootstrap.DropDownButton
import cz.payola.web.client.views.bootstrap.Icon
import cz.payola.web.client.views.elements.Text
import cz.payola.web.client.views.elements.Anchor
import cz.payola.web.client.views.elements.lists.ListItem

class CacheStorePresenter(val viewElement: html.Element, embeddedId: String, defaultPlugin: String, defaultCustomizationId: String) extends Presenter
{
    def initialize() {

    }

    def initializeAvailablePlugins() {
        val plugins = List(("", "")) ++ cz.payola.web.client.views.graph.AvailablePluginViews.getPlugins(None).map{ plugin =>
            ((plugin.name, plugin.getClass.getName.replaceAll(".", "_")))
        }

        initializeList(plugins, {e => setViewPlugin(embeddedId, e.target.value)}, defaultPlugin)
    }

    private def initializeList(listValues: List[(String, String)], event: (EventArgs[Field[String]] => Unit), selectedValue: String) : Select = {

        val dropDown = new Select("", "", "", listValues.map(p => new SelectOption(p._1, p._2)), "form-control")
        dropDown.changed += event
        val dropDownControl = new InputControl("", dropDown, None, None)

        dropDownControl.render(viewElement)
        dropDownControl.field.updateValue(selectedValue)

        dropDown
    }

    private def setViewPlugin(id: String, visualPlugin: String) {
        EmbeddingDescriptionManager.setViewPlugin(id, visualPlugin) { result => } { error => }
    }

    private def setCustomization(id: String, customizationId: String) {
        EmbeddingDescriptionManager.setCustomization(id, customizationId) { result => } { error => }
    }

    def initializeAvailableCustomizations() {
        val loaderIcon = new Icon(Icon.search, true)
        loaderIcon.setAttribute("class", "cacheCustomizationLoad glyphicon");
        loaderIcon.setAttribute("style", "width: 16px; height: 32px; display: inline-block; background-color: transparent;");
        loaderIcon.render(viewElement)

        val func = { customizationName: String =>

            removeChildren()
            val customizationsButton = initializeList(List((customizationName, defaultCustomizationId)), { e => }, defaultCustomizationId)
            customizationsButton.mousePressed += { e =>
                blockPage("Fetching public customizations...")
                CustomizationManager.getPublicCustomizations() { publicCust: cz.payola.web.shared.managers.PublicCustomizations =>
                    val custs = List(("", "")) ++ publicCust.ontologyCustomizations.map { ontoCust =>
                        ((ontoCust.name, ontoCust.id))
                    } ++ publicCust.userCustomizations.map { userCust => ((userCust.name, userCust.id)) }

                    removeChildren()
                    initializeList(custs, { e => setCustomization(embeddedId, e.target.value) }, defaultCustomizationId)

                    unblockPage()
                } { error => }
                true
            }
        }
        CustomizationManager.getCustomizationNameByID(defaultCustomizationId)
        { customizationName => func(customizationName) }
        { error => func("")}
    }

    private def removeChildren() {
        while(viewElement.hasChildNodes) {
            viewElement.removeChild(viewElement.firstChild)
        }
    }
}
