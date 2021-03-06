package spud.cms

class SpudPagePartial {
	def spudTemplateService
	def grailsApplication

	static belongsTo = [page: SpudPage]
	static transients = ['cachedContent']
	String name
	String symbolName
	String content
	String contentProcessed
	String format="html"

	String cachedContent

	Date dateCreated
	Date lastUpdated

	static mapping = {
		def cfg = it?.getBean('grailsApplication')?.config
		datasource(cfg?.spud?.core?.datasource ?: 'DEFAULT')

		cache true
		table 'spud_page_partials'
		autoTimestamp true
		content type:'text'
		contentProcessed type:'text'
		dateCreated column: 'created_at'
		lastUpdated column: 'updated_at'
	}
	static constraints = {
		contentProcessed nullable: true, maxSize: 65000
		content nullable:true, maxSize: 65000
		symbolName blank: false
		name blank:false
	}

	public void setName(String name) {
		this.name = name
		// this.symbolName = name.replaceAll(" ", "_").replaceAll(":","_").replaceAll("-","_").replaceAll(",","_").toLowerCase()
	}

	public void setContent(String _content) {
		content = _content
		this.contentProcessed = null
	}

	def beforeValidate() {
		if(this.content && !this.contentProcessed) {
			def formatter = grailsApplication.config.spud.formatters.find{ it.name == this.format}?.formatterClass
			if(formatter) {
				def formattedText = formatter.newInstance().compile(this.content)
				contentProcessed = formattedText
			} else {
				contentProcessed = this.content
			}
		}
	}

	public String render() {
		if(cachedContent) {
			return cachedContent
		}
		cachedContent = spudTemplateService.render("${page.name}.${name}",contentProcessed ?: content,[model: [page:page]])
	}
}
