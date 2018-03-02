# BitcoinPonziTool

##Instructions
###Indexes
After the execution on of the program, to achieve a reasonable speed you have to create some indexes.
Run the queries listed below (time ~45' each).

* db.transaction.createIndex("txid" : 1)
* db.transaction.createIndex("vin.address" : 1)
* db.transaction.createIndex("vout.addresses" : 1)
* db.transaction.createIndex("time" : 1)
* db.transaction.createIndex("blockHash" : 1)
* db.transaction.createIndex("isOpReturn" : 1)

###Schema
Transaction collection:
* txid (string): transaction id
* locktime (integer)
* vin (array of documents)
  - sequence (integer): transaction sequence number
  - coinbase (boolean): true for coinbase transactions
  - txid (string): id of the redeemed transaction
  - vout (integer): index of the redeemed output
  - scriptSig (string): script in bitcoinJ format
  - address (string): address of the redeemer (optional, works only for pay-to-publickey-hash
* vout (array of documents)
  - value (integer): output value in satoshi
  - n (integer): output index
  - scriptPubKey (string): script in bitcoinJ format
  - type (string): script type (available values P2PKH, PUB_KEY, P2SH, NO_TYPE)
  - isOpReturn (boolean): optional
  - opReturnData (string): bytes carried by the OP_RETURN, optional
  - addresses (array of string): addresses that can redeem the output (array to handle multisig scripts)
* blockhash (string): id of the containing block 
* blockheight (integer)
* time (long)
