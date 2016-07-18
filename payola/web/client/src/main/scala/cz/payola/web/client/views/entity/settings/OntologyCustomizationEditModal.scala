package cz.payola.web.client.views.entity.settings

import s2js.adapters.html
import cz.payola.common.entities.settings._
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.elements._
import cz.payola.web.client.events._
import cz.payola.web.client.views.bootstrap.element._
import cz.payola.web.client.presenters.entity.settings._
import cz.payola.web.client.views.elements.lists._
import cz.payola.web.client.views.elements.form.fields._
import cz.payola.common.visual.Color

class OntologyCustomizationEditModal(ontologyCustomization: OntologyCustomization)
    extends Modal("Edit ontology customization", Nil, Some("Done"), None, true, "large-modal", false)
{
    val classFillColorChanged = new UnitEvent[InputControl[_], ClassCustomizationEventArgs[InputControl[_]]]

    val classRadiusDelayedChanged = new UnitEvent[InputControl[_], ClassCustomizationEventArgs[InputControl[_]]]

    val classGlyphChanged = new UnitEvent[InputControl[_], ClassCustomizationEventArgs[InputControl[_]]]

    val propertyStrokeColorChanged = new UnitEvent[InputControl[_], PropertyCustomizationEventArgs[InputControl[_]]]

    val propertyStrokeWidthDelayedChanged =
        new UnitEvent[InputControl[_], PropertyCustomizationEventArgs[InputControl[_]]]

    val ontologyCustomizationName = new InputControl(
        "Name:",
        new TextInput("name", ontologyCustomization.name, "", "col-lg-6"),
        None, Some("col-lg-8")
    )

    ontologyCustomizationName.controlGroup.removeCssClass("form-group")

    val shareButtonViewSpace = new Span(Nil)

    val deleteButton = new Button(new Text("Delete"), "btn-danger", new Icon(Icon.remove))

    private val classCustomizationListItems = ontologyCustomization.classCustomizations.map {
        classCustomization =>
            val classListItem = new ListItem(List(new Anchor(List(
                new Icon(Icon.tag),
                new Text(uriToName(classCustomization.uri)))
            )))
            classListItem.mouseClicked += {
                e =>
                    onClassCustomizationSelected(classCustomization, classListItem)
                    false
            }
            classListItem
    }

    private val propertiesDiv = new Div(Nil, "col-lg-8")

    override val body = List(
        new Div(List(
            new Div(List(
                ontologyCustomizationName,
                new Div(List(shareButtonViewSpace), "btn-group inline-block pull-right").setAttribute(
                    "style", "display: inline; position: absolute; right: 5px;"),
                new Div(List(deleteButton), "btn-group inline-block pull-right").setAttribute(
                    "style", "display: inline; position: absolute; right: 165px;")),
                "row button-row"
            ).setAttribute("style", "padding-bottom: 5px;"),
            new Div(List(
                new Div(
                    List(new UnorderedList(classCustomizationListItems, "nav nav-list")),
                    "col-lg-4 modal-inner-view well no-padding"
                ).setAttribute("style", "padding: 8px 0; height: 100%"),
                propertiesDiv),
                "row"
            ).setAttribute("style", "height: 370px;")),
            "container-fluid"
        ).setAttribute("style", "padding: 0;margin-bottom: 40px;")
    )

    override def render(parent: html.Element) {
        super.render(parent)
        ontologyCustomization.classCustomizations.headOption.foreach {
            onClassCustomizationSelected(_, classCustomizationListItems.head)
        }
    }

    private def onClassCustomizationSelected(classCustomization: ClassCustomization, listItem: ListItem) {
        classCustomizationListItems.foreach(_.removeCssClass("active"))
        listItem.addCssClass("active")

        propertiesDiv.removeAllChildNodes()
        renderClassCustomizationViews(classCustomization)

        var propertiesDivs = List[Div]()
        classCustomization.propertyCustomizations.foreach{ propCustomization =>
            propertiesDivs = propertiesDivs ++ List(renderPropertyCustomizationViews(classCustomization, propCustomization))
        }
        val propertiesContainerDiv = new Div(propertiesDivs).setAttribute("style", "height:200px; overflow: auto;")
        propertiesContainerDiv.render(propertiesDiv.htmlElement)
    }

    private def renderClassCustomizationViews(classCustomization: ClassCustomization) {
        val fillColor = new InputControl(
            "Fill color:",
            new ColorInput("fillColor", Color(classCustomization.fillColor), ""), Some("col-lg-2"), Some("col-lg-10")
        )
        fillColor.controlGroup.setAttribute("style", "display: flex; margin: 0;")
        val radius = new InputControl(
            "Radius:",
            new NumericInput("radius", classCustomization.radius, "form-control").addCssClass("form-control").setAttribute("style", "width:200px;"), Some("col-lg-2"), Some("col-lg-10")
        )
        radius.controlGroup.setAttribute("style", "display: flex;")
        val glyph = new InputControl(
            "Glyph:",
            new GlyphInput("glyph", Some(classCustomization.glyph), "")  , Some("col-lg-2"), Some("col-lg-10")
        )
        glyph.controlGroup.setAttribute("style", "display: flex; margin: 0;")

        fillColor.delayedChanged += { _ =>
            classFillColorChanged.trigger(new ClassCustomizationEventArgs(fillColor, classCustomization,
                fillColor.field.value.map(_.toString).getOrElse("")))
        }
        radius.delayedChanged += { _ =>
            classRadiusDelayedChanged.trigger(new ClassCustomizationEventArgs(radius, classCustomization,
                radius.field.value.toString))
        }
        glyph.field.changed += { _ =>
            classGlyphChanged.trigger(new ClassCustomizationEventArgs(glyph, classCustomization,
                glyph.field.value.getOrElse("")))
        }

        val classDiv = new Div(List(fillColor, radius, glyph))
        classDiv.render(propertiesDiv.htmlElement)
    }

    private def renderPropertyCustomizationViews(classCustomization: ClassCustomization,
        propertyCustomization: PropertyCustomization): Div = {
        val headingDiv = new Div(List(new Text("Property " + uriToName(propertyCustomization.uri))), "label label-info")
        headingDiv.setAttribute("style", "padding: 5px; margin: 10px 0;")

        val strokeColor = new InputControl(
            "Stroke color:",
            new ColorInput("strokeColor", Color(propertyCustomization.strokeColor), ""), Some("col-lg-2"), Some("col-lg-10")
        )
        strokeColor.controlGroup.setAttribute("style", "display: flex; margin: 0;")
        val strokeWidth = new InputControl(
            "Stroke width:",
            new NumericInput("strokeWidth", propertyCustomization.strokeWidth, "form-control").addCssClass("form-control").setAttribute("style", "width:200px;"), Some("col-lg-2"), Some("col-lg-10")
        )
        strokeWidth.controlGroup.setAttribute("style", "display: flex;")

        strokeColor.delayedChanged += { _ =>
            propertyStrokeColorChanged.trigger(new PropertyCustomizationEventArgs(strokeColor, classCustomization,
                propertyCustomization, strokeColor.field.value.map(_.toString).getOrElse("")))
        }
        strokeWidth.delayedChanged += { _ =>
            propertyStrokeWidthDelayedChanged.trigger(new PropertyCustomizationEventArgs(strokeWidth,
                classCustomization, propertyCustomization, strokeWidth.field.value.toString))
        }

        val propertyDiv = new Div(List(headingDiv, strokeColor, strokeWidth))
        propertyDiv
    }

    private def uriToName(uri: String): String = {
        val nameParts = uri.split("#")
        if (nameParts.length > 1) nameParts(1) else uri
    }
}
