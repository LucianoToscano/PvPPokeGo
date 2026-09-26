package pvppokego.core.data

/**
 * Parser for localized move aliases used by OCR banner recognition.
 *
 * TSV format:
 * locale<TAB>alias<TAB>move_id
 */
object MoveAliasTable {
    fun fromTsv(tsv: String, locale: String): Map<String, String> {
        val wantedLocale = locale.trim().lowercase()
        return tsv.lineSequence()
            .drop(1)
            .filter { it.isNotBlank() && !it.trimStart().startsWith('#') }
            .mapNotNull { line ->
                val columns = line.split('\t')
                if (columns.size < 3) return@mapNotNull null
                val rowLocale = columns[0].trim().lowercase()
                val alias = columns[1].trim()
                val moveId = columns[2].trim().uppercase().replace(' ', '_')
                if (rowLocale != wantedLocale || alias.isBlank() || moveId.isBlank()) {
                    return@mapNotNull null
                }
                alias to moveId
            }
            .toMap()
    }
}
