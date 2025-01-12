# -*- coding: utf-8 -*-
#!/usr/bin/env python
# This is where to insert your generated API keys (http://api.telldus.com/keys)
pubkey = "FEHUVEW84RAFR5SP22RABURUPHAFRUNU"
privkey= "ZUXEVEGA9USTAZEWRETHAQUBUR69U6EF"
token ="" # Token Id
secret = "" # Token Secret
import requests, json, hashlib, uuid, time
localtime = time.localtime (time.time())
timestamp = str(time.mktime(localtime))
nonce = uuid.uuid4().hex
oauthSignature= (privkey + "%26" + secret)
# GET-request
response = requests.get(
url="https://pa-api.telldus.com/json/device/turnOn",
params={
# "id": "", add the actuator id
},
headers={
 "Authorization": 'OAuth oauth_consumer_key="{pubkey}", oauth_nonce="{nonce}", oauth_signature="{oauthSignature}", oauth_signature_method="PLAINTEXT", oauth_timestamp="{timestamp}", oauth_token="{token}", oauth_version="1$
 },
)
# Output/response from GET-request
responseData = response.json()
#print(responseData)
print(json.dumps (responseData, indent=4, sort_keys=True))