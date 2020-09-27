package info.camposha.pollux_app

/**
 * ANDROID: http://www.camposha.info : Oclemy.
 */
class Item(text1: String?,text2: String?,text3: String?,imageURL: String?) {
    var text1: String?=""
    var text2: String?=""
    var text3: String?=""
    var imageURL: String?=""

    init {
        this.text1 = text1
        this.text2 = text2
        this.text3 = text3
        this.imageURL = imageURL
    }
    override fun toString(): String {
        return text1!!
    }
}