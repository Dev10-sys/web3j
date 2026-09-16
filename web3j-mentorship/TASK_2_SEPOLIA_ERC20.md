1. Install Web3j CLI
2. Generate a ERC-20 smart contract project
3. Deploy it on Sepolia Network
4. Execute all the transactions available in the within the smart contracts.
5. Check the documentations and try randomly different features - make a note if anything is not working properly.
6. EVM basics - transactions, lifecylces, EIPs.
- create this manually 


# Tasks Status

* Installed Web3j using the Web3j CLI.

* Generated an ERC-20 smart contract project.
  * Minor issues found in generated test cases during execution.

* Deployed ERC-20 contract on Sepolia test network:
  * Created a new wallet using `web3j wallet create`.
  * Received Sepolia ETH using Google Cloud Sepolia Faucet.
  * Configured Alchemy Sepolia RPC endpoint.
  * Successfully deployed the contract.

  Contract Address:
  `0x0a9f7d0fba2f0dbf150e11edfc8cf99b050f72d5`
  
![Google Sepolia Faucet](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/01_google_faucet_site.png)

![Etherscan Faucet Tx](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/02_etherscan_faucet_tx.png)

![Etherscan Contract Deployment](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/03_etherscan_contract_deployment.png)


* Connected Java application with deployed ERC-20 contract using Web3j.
  * Loaded deployed contract instance.

* Tested ERC-20 read functions:
  * `name()`
  * `symbol()`
  * `decimals()`
  * `totalSupply()`
  * `balanceOf(address)`

![Java Code Connection](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/04_java_code_connection.png)

![Terminal Read Logs Part 1](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/05_terminal_read_logs_part1.png)

![Terminal Read Logs Part 2](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/06_terminal_read_logs_part2.png)


* Tested ERC-20 transaction functions:
  * `transfer()` — transferred tokens between wallets.
  
![Terminal Transfer Logs Part 1](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/07_terminal_transfer_logs_part1.png)

![Terminal Transfer Logs Part 2](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/08_terminal_transfer_logs_part2.png)


  * `approve()` — approved spender allowance.
  * `allowance()` — verified approved amount.  
  
![Terminal Approve Logs Part 1](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/09_terminal_approve_logs_part1.png)

![Terminal Approve Logs Part 2](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/10_terminal_approve_logs_part2.png)


  * `transferFrom()` — executed delegated transfer.

![Terminal TransferFrom Logs Part 1](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/11_terminal_transferFrom_logs_part1.png)

![Terminal TransferFrom Logs Part 2](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/12_terminal_transferFrom_logs_part2.png)


* Created and tested multiple wallets for ERC-20 flow:
  * Owner wallet (`0x55ff803ae7da9ccce482f929ffe7ba2a1bb10a48`)
  * Spender wallet (`0x0c36cd4821e46fa816fa86437baa91ee264bd7dc`)
  * Receiver wallet (`0x70997970C51812dc3A010C7d01b50e0d17dc79C8`)

![Java Code Spender Setup](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/13_java_code_spender_setup.png)

![Terminal Gas Funding Logs Part 1](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/14_terminal_gas_funding_logs_part1.png)

![Terminal Gas Funding Logs Part 2](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/15_terminal_gas_funding_logs_part2.png)


* Verified final token state:
  * Checked updated balances.
  * Checked remaining allowance after transferFrom.

![Terminal Final Allowance Check](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/16_terminal_final_allowance_check.png)


* Explored Web3j CLI wrapper generator functionality.
  * Generated wrapper for ERC-20 token

![Terminal CLI Wrapper Generation](https://raw.githubusercontent.com/Dev10-sys/web3j/docs/task-2-sepolia-erc20-deployment/web3j-mentorship/17_terminal_cli_wrapper_generation.png)  


* Studied official Ethereum developer documentation.

* Studied Ethereum Virtual Machine (EVM) and architecture:
  * Smart contract execution
  * Transactions
  * Gas mechanism
  * State changes
