package cz.payola.web.client.views.bootstrap.element

import cz.payola.web.client.views.elements.form._
import cz.payola.web.client.views.elements.form.fields._
import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.elements._

class GlyphInput(name: String, initialValue: Option[String], cssClass: String = "")
    extends ComposedView with Field[Option[String]]
{
    private val nothingSelectedText = ""

    private val glyphInput = new TextInput(name, if(initialValue.isDefined){initialValue.get}else{""}, "", "disabled input-mini glyph").setAttribute(
        "style", "width: 200px; display: inherit; line-height: normal;")
    glyphInput.setIsEnabled(false)

    private val addOn = new Span(List(new Text("Choose glyph")),"add-on btn btn-default").setAttribute("style", "margin-top: -15px; padding: 6px 12px 6px 12px !important;")
    private val clearBtn = new Button(new Text("Clear")).setAttribute("style", "margin-top: -15px;")

    clearBtn.mouseClicked += { e =>
        glyphInput.value = nothingSelectedText
        changed.triggerDirectly(this)
        false
    }

    private val appendSpan = new Span(List(glyphInput, addOn, clearBtn), "input-append")

    val charSeq = List(
        "\u0020","\u0021","""\u0022""","\u0023","\u0024","\u0025","\u0026","\u0027","\u0028","\u0029","\u002A","\u002B",
        "\u002C","\u002D","\u002E","\u002F","\u0030","\u0031","\u0032","\u0033","\u0034","\u0035","\u0036","\u0037",
        "\u0038","\u0039","\u003A","\u003B","\u003C","\u003D","\u003E","\u003F","\u0040","\u0041","\u0042","\u0043",
        "\u0044","\u0045","\u0046","\u0047","\u0048","\u0049","\u004A","\u004B","\u004C","\u004D","\u004E","\u004F",
        "\u0050","\u0051","\u0052","\u0053","\u0054","\u0055","\u0056","\u0057","\u0058","\u0059","\u005A","\u005B",
        """\u005C""","\u005D","\u005E","\u005F","""\u0060""","\u0061","\u0062","\u0063","\u0064","\u0065","\u0066","\u0067",
        "\u0068","\u0069","\u006A","\u006B","\u006C","\u006D","\u006E","\u006F","\u0070","\u0071","\u0072","\u0073",
        "\u0074","\u0075","\u0076","\u0077","\u0078","\u0079","\u007A","\u007B","\u007C","\u007D","\u007E","\u00A9",
        "\u00AE","\u00C4","\u00C5","\u00C7","\u00C9","\u00D1","\u00D6","\u00DC","\u00E0","\u00E1","\u00E2","\u00E3",
        "\u00E4","\u00E5","\u00E7","\u00E8","\u00E9","\u00EA","\u00EB","\u00EC","\u00ED","\u00EE","\u00EF","\u00F1",
        "\u00F2","\u00F3","\u00F4","\u00F6"
    )

    def closePopup() {
        updateDisplay("none")
    }

    def openPopup() {
        updateDisplay("block")
    }

    private def updateDisplay(newStatus: String) {
        val clearedStyle = if(glyphDiv.getAttribute("style") != null && glyphDiv.getAttribute("style").indexOf("display") != -1) {
            val oldStyle = glyphDiv.getAttribute("style")
            val newStyle1 = oldStyle.substring(0, oldStyle.indexOf("display"))
            val newStyle2 = oldStyle.substring(oldStyle.indexOf(";", oldStyle.indexOf("display")) +1)

            newStyle1 + newStyle2
        } else {
            glyphDiv.getAttribute("style")
        }

        glyphDiv.setAttribute("style", "display:"+newStatus+";"+clearedStyle)
    }

    val spans = charSeq.map{ num =>
        val span = new Span(List(new Text(num)), "glyph").setAttribute("style", "font-size: 40px; line-height: normal;")
        span.mouseClicked += { e =>
            glyphInput.updateValue(num)
            changed.triggerDirectly(this)
            closePopup()
            false
        }
        span
    }

    val glyphDiv = new Div(spans,"glyph-popup dropdown-menu")
    private val wrap = new Div(List(appendSpan,glyphDiv),"glyph-wrapper").setAttribute("style","padding-bottom: 25px;")

    addOn.mouseClicked += { e =>
        openPopup()
        false
    }

    def formHtmlElement = glyphInput.htmlElement

    def createSubViews = List(
        wrap
    )

    def value: Option[String] = {
        if (glyphInput.value == nothingSelectedText) {
            None
        } else {
            Some(glyphInput.value)
        }
    }

    protected def updateValue(newValue: Option[String]) {
        glyphInput.updateValue(newValue.map(_.toString).getOrElse(nothingSelectedText))
    }

    def isActive = glyphInput.isActive

    def isActive_=(newValue: Boolean) {
        glyphInput.isActive = newValue
    }
}
