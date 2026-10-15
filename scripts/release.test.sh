#!/usr/bin/env bash
# Tests release.sh without GitHub: origin is a local bare repo, and gh is a stub on PATH
# that records its arguments.
#
# Worth testing is what a release gets silently wrong: building the branch instead of a
# tag that exists, taking an unreachable origin for "no tag yet", and creating a release
# a second time instead of replacing its file.
set -euo pipefail

release="$(cd "$(dirname "$0")" && pwd)/release.sh"
t=$(mktemp -d)
trap 'rm -rf "$t"' EXIT
fails=0
check() { # check <what> [!] <command...>: ok when the command succeeds, or fails after a !
	local what=$1 want=0 got=0
	shift
	if [ "$1" = "!" ]; then
		want=1
		shift
	fi
	"$@" >/dev/null 2>&1 || got=1
	if [ "$got" = "$want" ]; then echo "ok   $what"; else
		echo "FAIL $what"
		fails=$((fails + 1))
	fi
}

# origin has beta-1 on the first commit and main one commit later. The clone is the
# runner's: main only, shallow, no tags.
git init -q -b main "$t/seed"
git -C "$t/seed" -c user.name=t -c user.email=t@t commit -q --allow-empty -m one
git -C "$t/seed" tag beta-1
git -C "$t/seed" -c user.name=t -c user.email=t@t commit -q --allow-empty -m two
git clone -q --bare "$t/seed" "$t/origin.git"
git clone -q --depth=1 --no-tags "file://$t/origin.git" "$t/repo"
cd "$t/repo"
tagged=$(git -C "$t/seed" rev-parse beta-1)
main=$(git rev-parse HEAD)

check "a missing tag is not an error" "$release" checkout beta-2
check "a missing tag leaves the branch checked out" test "$(git rev-parse HEAD)" = "$main"
check "an existing tag is found" "$release" checkout beta-1
check "an existing tag is checked out as it is" test "$(git rev-parse HEAD)" = "$tagged"

git checkout -q main
git remote set-url origin "file://$t/gone.git"
check "an unreachable origin stops the run, not taken for a new tag" ! "$release" checkout beta-1

mkdir "$t/bin"
cat >"$t/bin/gh" <<'STUB'
#!/usr/bin/env bash
echo "$*" >>"$CALLS"
[ "$1 $2" != "release view" ] || [ "${RELEASE:-}" = exists ]
STUB
chmod +x "$t/bin/gh"
export PATH="$t/bin:$PATH" CALLS="$t/calls"

: >"$CALLS"
"$release" publish beta-2 Ofis.dmg >/dev/null
check "a missing release is created as a pre-release, its tag on HEAD" \
	grep -qx "release create beta-2 --target $main --title Ofis beta-2 --prerelease --generate-notes" "$CALLS"
check "and gets the file" grep -qx "release upload beta-2 Ofis.dmg --clobber" "$CALLS"

: >"$CALLS"
RELEASE=exists "$release" publish beta-1 Ofis.dmg >/dev/null
check "an existing release is not created again" ! grep -q "release create" "$CALLS"
check "its file is replaced" grep -qx "release upload beta-1 Ofis.dmg --clobber" "$CALLS"

exit $((fails > 0))
