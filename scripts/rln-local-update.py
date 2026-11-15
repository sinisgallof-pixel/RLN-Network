from hashlib import sha256
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from json import dumps
from pathlib import Path
from shutil import copyfileobj

ADDRESS = ("127.0.0.1", 8766)
BASE_URL = f"http://{ADDRESS[0]}:{ADDRESS[1]}"
VERSION = "2.0.3-qa.1"
APK = Path(__file__).resolve().with_name(f"RLN-Network-{VERSION}.apk")


class UpdateHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/releases":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(metadata)))
            self.end_headers()
            self.wfile.write(metadata)
        elif self.path == f"/{APK.name}":
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Length", str(APK.stat().st_size))
            self.end_headers()
            with APK.open("rb") as source:
                copyfileobj(source, self.wfile)
        else:
            self.send_error(404)


if __name__ == "__main__":
    if not APK.is_file():
        raise SystemExit(f"File mancante: {APK.name}")
    with APK.open("rb") as source:
        digest = sha256()
        for block in iter(lambda: source.read(65536), b""):
            digest.update(block)
    metadata = dumps([{
        "tag_name": f"v{VERSION}",
        "name": f"RLN Network {VERSION}",
        "body": "Prova locale di aggiornamento sul MagicPad 2.",
        "html_url": f"{BASE_URL}/releases",
        "draft": False,
        "prerelease": False,
        "assets": [{
            "name": APK.name,
            "state": "uploaded",
            "size": APK.stat().st_size,
            "digest": f"sha256:{digest.hexdigest()}",
            "browser_download_url": f"{BASE_URL}/{APK.name}",
        }],
    }]).encode()
    with ThreadingHTTPServer(ADDRESS, UpdateHandler) as server:
        print("Server di prova attivo sul tablet. Lascia aperto Termux.", flush=True)
        server.serve_forever()
