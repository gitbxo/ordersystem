
# python3 src/simulator/simulator.py Downloads/dispatch_orders.json

import json
import requests
import sys


def createOrder(order):
  requests.post(f"http://localhost:9000/item/test?itemId={order['id']}&name={order['name']}&prepareMillis={order['prepTime']}000&expiryMillis=300000")
  requests.post(f"http://localhost:9000/order/test?orderId={order['id']}")
  requests.put(f"http://localhost:9000/order/item?orderId={order['id']}&itemId={order['id']}&quantity=1")
  requests.put(f"http://localhost:9000/order/submit?orderId={order['id']}")


def simulator(filename):
  with open(filename) as f:
    for data in json.loads(f.read()):
      createOrder(data)


if __name__ == '__main__':
  if len(sys.argv) > 1 and sys.argv[1]:
    simulator(sys.argv[1])
  else:
    print('pass orders.json filename as parameter')
    print(f'e.g. python3 {sys.argv[0]} Downloads/orders.json')
