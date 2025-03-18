package com.topstreams.firetv

data class NbaTeam(
    val id: String,
    val name: String,
    val abbreviation: String,
    val city: String,
    val fullName: String,
    val streamUrl: String
) {
    companion object {
        fun getAllTeams(): List<NbaTeam> {
            return listOf(
                NbaTeam("atl", "Hawks", "ATL", "Atlanta", "Atlanta Hawks", "https://topstreams.info/nba/hawks"),
                NbaTeam("bos", "Celtics", "BOS", "Boston", "Boston Celtics", "https://topstreams.info/nba/celtics"),
                NbaTeam("bkn", "Nets", "BKN", "Brooklyn", "Brooklyn Nets", "https://topstreams.info/nba/nets"),
                NbaTeam("cha", "Hornets", "CHA", "Charlotte", "Charlotte Hornets", "https://topstreams.info/nba/hornets"),
                NbaTeam("chi", "Bulls", "CHI", "Chicago", "Chicago Bulls", "https://topstreams.info/nba/bulls"),
                NbaTeam("cle", "Cavaliers", "CLE", "Cleveland", "Cleveland Cavaliers", "https://topstreams.info/nba/cavaliers"),
                NbaTeam("dal", "Mavericks", "DAL", "Dallas", "Dallas Mavericks", "https://topstreams.info/nba/mavericks"),
                NbaTeam("den", "Nuggets", "DEN", "Denver", "Denver Nuggets", "https://topstreams.info/nba/nuggets"),
                NbaTeam("det", "Pistons", "DET", "Detroit", "Detroit Pistons", "https://topstreams.info/nba/pistons"),
                NbaTeam("gsw", "Warriors", "GSW", "Golden State", "Golden State Warriors", "https://topstreams.info/nba/warriors"),
                NbaTeam("hou", "Rockets", "HOU", "Houston", "Houston Rockets", "https://topstreams.info/nba/rockets"),
                NbaTeam("ind", "Pacers", "IND", "Indiana", "Indiana Pacers", "https://topstreams.info/nba/pacers"),
                NbaTeam("lac", "Clippers", "LAC", "Los Angeles", "LA Clippers", "https://topstreams.info/nba/clippers"),
                NbaTeam("lal", "Lakers", "LAL", "Los Angeles", "Los Angeles Lakers", "https://topstreams.info/nba/lakers"),
                NbaTeam("mem", "Grizzlies", "MEM", "Memphis", "Memphis Grizzlies", "https://topstreams.info/nba/grizzlies"),
                NbaTeam("mia", "Heat", "MIA", "Miami", "Miami Heat", "https://topstreams.info/nba/heat"),
                NbaTeam("mil", "Bucks", "MIL", "Milwaukee", "Milwaukee Bucks", "https://topstreams.info/nba/bucks"),
                NbaTeam("min", "Timberwolves", "MIN", "Minnesota", "Minnesota Timberwolves", "https://topstreams.info/nba/timberwolves"),
                NbaTeam("nop", "Pelicans", "NOP", "New Orleans", "New Orleans Pelicans", "https://topstreams.info/nba/pelicans"),
                NbaTeam("nyk", "Knicks", "NYK", "New York", "New York Knicks", "https://topstreams.info/nba/knicks"),
                NbaTeam("okc", "Thunder", "OKC", "Oklahoma City", "Oklahoma City Thunder", "https://topstreams.info/nba/thunder"),
                NbaTeam("orl", "Magic", "ORL", "Orlando", "Orlando Magic", "https://topstreams.info/nba/magic"),
                NbaTeam("phi", "76ers", "PHI", "Philadelphia", "Philadelphia 76ers", "https://topstreams.info/nba/76ers"),
                NbaTeam("phx", "Suns", "PHX", "Phoenix", "Phoenix Suns", "https://topstreams.info/nba/suns"),
                NbaTeam("por", "Trail Blazers", "POR", "Portland", "Portland Trail Blazers", "https://topstreams.info/nba/blazers"),
                NbaTeam("sac", "Kings", "SAC", "Sacramento", "Sacramento Kings", "https://topstreams.info/nba/kings"),
                NbaTeam("sas", "Spurs", "SAS", "San Antonio", "San Antonio Spurs", "https://topstreams.info/nba/spurs"),
                NbaTeam("tor", "Raptors", "TOR", "Toronto", "Toronto Raptors", "https://topstreams.info/nba/raptors"),
                NbaTeam("uta", "Jazz", "UTA", "Utah", "Utah Jazz", "https://topstreams.info/nba/jazz"),
                NbaTeam("was", "Wizards", "WAS", "Washington", "Washington Wizards", "https://topstreams.info/nba/wizards")
            )
        }
    }
}