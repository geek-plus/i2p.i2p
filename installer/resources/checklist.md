# Release checklist

## One week before

- Make announcement on Transifex with checkin deadline


## A day or two before

1. Write the release announcement and push to Transifex:

  - Checkout i2p.newsxml branch
    - See README for setup
  - `./create_new_entry.sh`
    - Entry href should be the in-net link to the release blog post
  - `tx push -s`
  - `mtn ci`

2. Write the draft blog post and push to Transifex:

  - Checkout i2p.www branch
  - Write draft release announcement - see i2p2www/blog/README for instructions
    - Top content should be the same as the news entry
  - `tx push -s`
  - `mtn ci`

3. Make announcement on Transifex asking for news translation


## On release day

### Preparation

1. Ensure all translation updates are imported from Transifex

2. Sync with mtn.i2p2.i2p

3. Start with a clean checkout:

    ```
    mtn -d i2p.mtn co --branch=i2p.i2p /path/to/releasedir
    ```

  - You may build with Java 7 or higher, but ensure you have the Java 6 JRE installed for the bootclasspath

4. Create override.properties with (adjust as necessary):

    ```
    release.privkey.su3=/path/to/su3keystore.ks
    release.gpg.keyid=0xnnnnnnnn
    release.signer.su3=xxx@mail.i2p
    build.built-by=xxx
    javac.compilerargs=-bootclasspath /usr/lib/jvm/java-6-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-6-openjdk-amd64/jre/lib/jce.jar
    ```

5. Copy latest trust list _MTN/monotonerc from website or some other workspace

6. Change revision in:
  - `history.txt`
  - `installer/install.xml`
  - `core/java/src/net/i2p/CoreVersion.java`
  - `router/java/src/net/i2p/router/RouterVersion.java`
    - (change to BUILD = 0 and EXTRA = "")

7. `mtn ci`

8. Review the complete diff from the last release:

    ```
    mtn diff -r t:i2p-0.9.(xx-1) > out.diff
    vi out.diff
    ```

9. Verify that no untrusted revisions were inadvertently blessed by a trusted party:

    ```
    mtn log --brief --no-graph --to t:i2p-0.9.(xx-1) | cut -d ' ' -f 2 | sort | uniq -c
    ```

### Build and test

1. `ant release`

    > NOTE: These tasks are now automated by `ant release`
    >
    > Build and tag:
    >
    >     ant pkg
    >
    > Create signed update files with:
    >
    >     export I2P=~/i2p
    >     java -cp $I2P/lib/i2p.jar net.i2p.crypto.TrustedUpdate sign i2pupdate.zip i2pupdate.sud /path/to/private.key 0.x.xx
    >     java -cp $I2P/lib/i2p.jar net.i2p.crypto.TrustedUpdate sign i2pupdate200.zip i2pupdate.su2 /path/to/private.key 0.x.xx
    >
    > Verify signed update files with:
    >
    >     java -cp $I2P/lib/i2p.jar net.i2p.crypto.TrustedUpdate showversion i2pupdate.sud
    >     java -cp $I2P/lib/i2p.jar net.i2p.crypto.TrustedUpdate verifysig i2pupdate.sud
    >
    > Make the source tarball:
    >
    >     Start with a clean checkout mtn -d i2p.mtn co --branch=i2p.i2p i2p-0.x.xx
    >     Double-check trust list
    >     tar cjf i2psource-0.x.xx.tar.bz2 --exclude i2p-0.x.xx/_MTN i2p-0.x.xx
    >     mv i2p-0.x.xx.tar.bz2 i2p.i2p
    >
    > Rename some files:
    >
    >     mv i2pinstall.exe i2pinstall-0.x.xx.exe
    >     mv i2pupdate.zip i2pupdate-0.x.xx.zip
    >
    > Generate hashes:
    >
    >     sha256sum i2p*0.x.xx.*
    >     sha256sum i2pupdate.sud
    >     sha256sum i2pupdate.su2
    >
    > Generate PGP signatures:
    >
    >     gpg -b i2pinstall-0.x xx.exe
    >     gpg -b i2psource-0.x.xx.tar.bz2
    >     gpg -b i2pupdate-0.x.xx.zip
    >     gpg -b i2pupdate.sud
    >     gpg -b i2pupdate.su2
    >
    > (end of tasks automated by 'ant release')

2. Now test:
  - Save the output about checksums, sizes, and torrents to a file
    (traditionally `shasums.txt`)
    - (edit timestamps to UTC if you care)
  - Copy all the release files somewhere, make sure you have the same ones as last release
  - Verify sha256sums for release files
  - Check file sizes vs. previous release, shouldn't be smaller
    - If the update includes GeoIP, it will be about 1MB bigger
  - Unzip or list files from `i2pupdate.zip`, see if it looks right
  - For either windows or linux installer: (probably should do both the first time)
    - Rename any existing config dir (e.g. mv .i2p .i2p-save)
    - Run installer, install to temp dir
    - Look in temp dir, see if all the files are there
    - Unplug ethernet / turn off wifi so RI doesn't leak
    - `i2prouter start`
    - Verify release number in console
    - Verify welcome news
    - Click through all the app, status, eepsite, and config pages, see if they look right
    - Click through each of the translations, see if /console looks right
    - Look for errors in /log (other than can't reseed errors)
    - Look in config dir, see if all the files are there
    - Shutdown
    - Delete config dir
    - Move saved config dir back
    - Reconnect ethernet / turn wifi back on
  - Load torrents in i2psnark on your production router, verify infohashes

3. If all goes well, tag and sync the release:

    ```
    mtn tag h: i2p-0.x.xx
    mtn cert t:i2p-0.x.xx branch i2p.i2p.release
    mtn sync (with e.g. mtn.killyourtv.i2p)
    ```

### Distribute updates

1. Update news with new version:
  - Add magnet links, change release dates and release number in to old-format
    news.xml, and distribute to news hosts
  - In the i2p.newsxml branch, edit magnet links, release dates and release
    number in data/releases.json, and check in

2. Add update torrents to tracker2.postman.i2p and start seeding (su2 and su3)

3. Notify the following people:
  - All in-network update hosts
  - PPA maintainer
  - news.xml maintainer
  - backup news.xml maintainer
  - website files maintainer

4. Update Trac:
  - Add milestone and version dates
  - Increment milestone and version defaults

5. Wait for a few update hosts to be ready

6. Tell news hosts to flip the switch

### Notify release

1. Wait for files to be updated on download server

2. Website files to change:
  - Sync with mtn.i2p-projekt.i2p
  - `i2p2www/static/hosts.txt` if it changed (copy from i2p.i2p mtn branch)
  - `i2p2www/__init__.py` (release number)
  - `i2p2www/pages/downloads/list.html` (release signer)
  - `i2p2www/pages/downloads/macros` (checksums)
  - `i2p2www/static/news/news.xml`
  - Sync with mtn.i2p-projekt.i2p

3. Wait for debian packages to be ready

4. Announce on:
  - #i2p, #i2p-dev (also on Freenode side)
  - forum.i2p
  - Twitter
