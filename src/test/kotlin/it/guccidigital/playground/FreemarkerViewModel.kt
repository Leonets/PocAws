package it.guccidigital.playground

import org.http4k.template.ViewModel

data class FreemarkerViewModel(val description: String) : ViewModel {
    override fun template() = super.template() + ".html"
}
