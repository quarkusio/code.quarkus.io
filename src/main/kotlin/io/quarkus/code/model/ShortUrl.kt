package io.quarkus.code.model

import me.nimavat.shortid.ShortId

data class ShortUrl(val id: String = ShortId.generate(), val url: String)