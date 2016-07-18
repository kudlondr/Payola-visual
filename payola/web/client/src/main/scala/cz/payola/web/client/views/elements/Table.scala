package cz.payola.web.client.views.elements

import s2js.adapters.html
import cz.payola.web.client.views._
import cz.payola.web.client.View

class Table(subViews: Seq[View] = Nil, cssClass: String = "")
    extends ElementView[html.Element]("table", subViews, cssClass)
