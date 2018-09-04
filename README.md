# Header golf

Inspired by the concept of [code golf](https://en.wikipedia.org/wiki/Code_golf), 
this project aims to achieve the most compact encoding of Bitcoin's 
[header chain](https://bitcoin.org/en/glossary/header-chain).

## Running

If you have a full node running with the REST option enabled, 
you can get a copy of the header chain by running:

    $ gradle run
    
Once you have a copy of the header chain, you can run:

    $ gradle build
    $ java -jar build/libs/headergolf-0.1.0.jar -i headers.dat -o compressed.dat

## Heuristics

### Previous block hash

The previous block hash doesn't need to be stored as it can be computed
from the previous header. 

As the very first header's previous hash is zero-filled, 
we don't even need to store the first one:

    prevHashN = sha256(sha256(headerN-1))
    prevHash0 = 0x000000....0000000000000
     
### Version numbers

Since version numbers are mostly 1 or 2, they can be efficiently compressed
using [FastFPOR](https://github.com/lemire/FastPFor).


## Further improvements

(inspired by [this email](https://lists.linuxfoundation.org/pipermail/bitcoin-dev/2017-December/015385.html))

 * Include the bits only on headers with a height that is a multiple of 2016
since it does not change in between. 

 *  Compress time to two bytes by using the bounds on allowed values 
from the consensus rules.

## Results

These results were obtained for the header chain at block 
[#539964](https://www.blockchain.com/btc/block/0000000000000000000d0a4281ca43fc936fbd7c957a06a7354e179333421c32).
The raw file is 43,197,200 bytes.

| Algorithm  | Compressed size  | Compression ratio |
| :--------- | ---------------: | ----------------: |
| GZIP       | 36,978,199 bytes |             1.168 |
| LZMA       | 35,619,153 bytes |             1.213 | 
| 7z         | 35,602,912 bytes |             1.213 |
| headergolf | 24,370,616 bytes |             1.772 |