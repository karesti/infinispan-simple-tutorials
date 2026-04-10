#!/usr/bin/env python3
"""
Digest authentication helper for Infinispan MCP endpoint.
Used by Claude Code's headersHelper to compute Digest auth headers.
"""
import hashlib, json, os, re, secrets
from urllib.request import urlopen, Request
from urllib.error import HTTPError

url = os.environ.get("INFINISPAN_URL", "http://localhost:11222/rest/v3/mcp")
username = os.environ.get("INFINISPAN_USER", "admin")
password = os.environ.get("INFINISPAN_PASS", "password")

# Step 1: Get the digest challenge from the server
try:
    urlopen(Request(url, method="GET"))
except HTTPError as e:
    if e.code == 401:
        auth_header = e.headers.get("WWW-Authenticate", "")
    else:
        raise
else:
    # No auth required, return empty headers
    print(json.dumps({}))
    exit(0)

# Step 2: Parse the challenge fields
def parse_field(header, name):
    match = re.search(rf'{name}="?([^",]+)"?', header)
    return match.group(1) if match else None

realm = parse_field(auth_header, "realm")
nonce = parse_field(auth_header, "nonce")
opaque = parse_field(auth_header, "opaque")
algorithm = parse_field(auth_header, "algorithm") or "MD5"
qop = parse_field(auth_header, "qop")

# Step 3: Compute the digest response
uri = "/rest/v3/mcp"
nc = "00000001"
cnonce = secrets.token_hex(16)

hash_fn = hashlib.sha256 if "256" in algorithm else hashlib.md5

ha1 = hash_fn(f"{username}:{realm}:{password}".encode()).hexdigest()
ha2 = hash_fn(f"POST:{uri}".encode()).hexdigest()

if qop:
    response = hash_fn(
        f"{ha1}:{nonce}:{nc}:{cnonce}:{qop}:{ha2}".encode()
    ).hexdigest()
    auth_value = (
        f'Digest username="{username}", realm="{realm}", nonce="{nonce}", '
        f'uri="{uri}", algorithm={algorithm}, qop={qop}, nc={nc}, '
        f'cnonce="{cnonce}", response="{response}"'
    )
else:
    response = hash_fn(f"{ha1}:{nonce}:{ha2}".encode()).hexdigest()
    auth_value = (
        f'Digest username="{username}", realm="{realm}", nonce="{nonce}", '
        f'uri="{uri}", algorithm={algorithm}, response="{response}"'
    )

if opaque:
    auth_value += f', opaque="{opaque}"'

print(json.dumps({"Authorization": auth_value}))