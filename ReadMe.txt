
SEND POST REQUEST to URL (service address)

These params should be in application.properties
  "login":"some_login",
  "password":"some_password",


http://XXX.XX.XXX.ZZZ:9095/sms/send

{
  "login":"some_login",
  "password":"some_password",
  "msisdn":"996773905665", //PHONE NUMBER

  "text": "SMS TEXT TO RECEIVER"
}
