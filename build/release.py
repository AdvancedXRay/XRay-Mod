# This should:
#   Open and use the release.md file
#   Push to Curse api & Github's Release api
#   Display status of release and allow retry of single deploy lane
#   Allow title input
#   Allow version number replacement
#   Edit the version.json in master to reflect the release

#               MCVERSION-VERSION-RELEASENUMBER
#               mc1.12.2-1.4.5-r1
versionSchema = "mc%s-%s-r%d"
